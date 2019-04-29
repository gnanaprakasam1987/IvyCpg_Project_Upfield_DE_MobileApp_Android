package com.ivy.sd.png.provider;

import android.content.Context;
import android.os.Environment;
import android.text.TextPaint;

import com.ivy.cpg.view.collection.CollectionHelper;
import com.ivy.cpg.view.order.OrderHelper;
import com.ivy.cpg.view.order.discount.DiscountHelper;
import com.ivy.cpg.view.order.scheme.SchemeDetailsMasterHelper;
import com.ivy.cpg.view.order.tax.TaxBO;
import com.ivy.cpg.view.salesreturn.SalesReturnReasonBO;
import com.ivy.cpg.view.van.vanunload.VanUnLoadModuleHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.BomReturnBO;
import com.ivy.sd.png.bo.LoadManagementBO;
import com.ivy.sd.png.bo.PaymentBO;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.SchemeBO;
import com.ivy.sd.png.bo.SchemeProductBO;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.bo.StockReportBO;
import com.ivy.sd.png.commons.NumberToWord;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.StandardListMasterConstants;
import com.ivy.utils.DateTimeUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

/**
 * Created by subramanian.r on 03-02-2016.
 */
public class CommonPrintHelper {
    private Context context;
    private BusinessModel bmodel;
    private static CommonPrintHelper instance = null;
    private OrderHelper orderHelper;

    private XmlPullParserFactory xmlFactoryObject;
    private XmlPullParser xmlParser;

    private int mPaperLenghtInChar;
    private int mGlobalPrecision;


    private static String ALIGNMENT_RIGHT = "RIGHT";
    private static String ALIGNMENT_LEFT = "LEFT";
    private static String ALIGNMENT_CENTER = "CENTER";

    private static String TAG_TITLE = "title";

    private static String TAG_DATE = "date";
    private static String TAG_TIME = "time";
    private static String TAG_DELIVERY_DATE = "delivery_date";

    private static String TAG_INVOICE_NUMBER = "invoice_number";
    private static String TAG_ORDER_NUMBER = "order_number";

    private static String TAG_DISTRIBUTOR_NAME = "dist_name";
    private static String TAG_DISTRIBUTOR_ADDRESS1 = "dist_address1";
    private static String TAG_DISTRIBUTOR_ADDRESS2 = "dist_address2";
    private static String TAG_DISTRIBUTOR_ADDRESS3 = "dist_address3";
    private static String TAG_DISTRIBUTOR_CONTACT_NUMBER = "dist_number";
    private static String TAG_DISTRIBUTOR_TIN_NUMBER = "dist_tin";
    private static String TAG_DISTRIBUTOR_CST_NUMBER = "dist_cst";
    private static String TAG_DISTRIBUTOR_FAX_NUMBER = "dist_fax_no";
    private static String TAG_DISTRIBUTOR_GST_NUMBER = "dist_gst_no";
    private static String TAG_SUPPLIER_NAME = "supplier_name";

    private static String TAG_BEAT_CODE = "beat_code";

    private static String TAG_RETAILER_NAME = "ret_name";
    private static String TAG_RETAILER_CODE = "ret_code";
    private static String TAG_RETAILER_ADDRESS1 = "ret_address1";
    private static String TAG_RETAILER_ADDRESS2 = "ret_address2";
    private static String TAG_RETAILER_ADDRESS3 = "ret_address3";
    private static String TAG_RETAILER_CITY = "ret_city";
    private static String TAG_RETAILER_PIN_CODE = "ret_pin_code";
    private static String TAG_RETAILER_CONTACT_NUMBER = "ret_number";
    private static String TAG_RETAILER_TIN_NUMBER = "ret_tin";
    private static String TAG_RETAILER_CST_NUMBER = "ret_cst";
    private static String TAG_RETAILER_GST_NUMBER = "ret_gst_no";
    private static String TAG_RETAILER_ROUTE = "ret_route";

    private static String TAG_SELLER_CODE = "seller_code";
    private static String TAG_SELLER_NAME = "seller_name";
    private static String TAG_SELLER_ID = "seller_id";

    private static String TAG_PRODUCT_CODE = "prod_code";
    private static String TAG_PRODUCT_NAME = "prod_name";
    private static String TAG_HSN_CODE = "hsn_code";

    private static String TAG_PRODUCT_PRICE_CASE = "prod_price_case";
    private static String TAG_PRODUCT_PRICE_OUTER = "prod_price_outer";
    private static String TAG_PRODUCT_PRICE_PIECE = "prod_price_piece";
    private static String TAG_DISCOUNTED_PRICE = "prod_discounted_price_piece";

    private static String TAG_PRODUCT_MRP = "prod_mrp";

    private static String TAG_PRODUCT_QTY_CASE = "prod_qty_case";
    private static String TAG_PRODUCT_QTY_OUTER = "prod_qty_outer";
    private static String TAG_PRODUCT_QTY_PIECE = "prod_qty_piece";
    private static String TAG_PRODUCT_QTY_TOTAL_IN_PIECE = "prod_qty_total_piece";
    private static String TAG_PRODUCT_SCHEME_DISCOUNT = "prod_scheme_discount";
    private static String TAG_PRODUCT_UOM_WISE_QTY = "prod_uom_wise_qty";

    private static String TAG_PRODUCT_LINE_VALUE = "prod_line_value";
    private static String TAG_PRODUCT_LINE_VALUE_EXCLUDING_TAX = "prod_line_value_excl_tax";
    private static String TAG_PRODUCT_lINE_VALUE_INCLUDING_TAX = "prod_line_value_incl_tax";
    private static String TAG_PRODUCT_TAX_PERCENTAGE = "prod_line_tax_percentage";
    private static String TAG_PRODUCT_TAX_VALUE = "prod_line_tax_value";

    private static String TAG_PRODUCT_TAG_DESC = "prod_tag_desc";

    private static String TAG_PRODUCT_FOC = "prod_foc";

    //Project specific promo type
    private static String TAG_PRODUCT_PROMO_TYPE = "prod_promo_type";

    private int mProductCaseQtyTotal;
    private int mProductPieceQtyTotal;
    private int mProductOuterQtyTotal;
    private int mProductQtyInPieceTotal;
    private double mProductLineValueTotal;
    private double mProductLineValueIncludingTaxTotal;
    private double mProductLineValueExcludingTaxTotal;

    private int mProductRetQtyTotal;
    private int mProductRepQtyTotal;
    private int mProductRepOrdInPieceTotal;

    private static String TAG_PRODUCT_REPLACE_QTY_PIECE = "prod_qty_replace_piece";
    private static String TAG_PRODUCT_RETURN_QTY_PIECE = "prod_qty_return_piece";
    private static String TAG_PRODUCT_SUM_QTY_PIECE_WITH_REP = "prod_qty_total_piece_with_rep";

    private static String TAG_PRODUCT_LINE_TOTAL = "line_total";
    private static String TAG_PRODUCT_LINE_EXCLUDING_TAX_TOTAL = "line_excl_tax_total";
    private static String TAG_PRODUCT_LINE_TOTAL_WITH_QTY = "line_total_with_qty";
    private static String TAG_PRODUCT_LINE_TOTAL_QTY = "total_qty";

    private static String TAG_DISCOUNT_PRODUCT_PRICE_OFF = "discount_product_price_off";
    private static String TAG_DISCOUNT_PRODUCT_APPLY = "discount_product_apply";
    private static String TAG_DISCOUNT_PRODUCT_ENTRY = "discount_product_entry";
    private static String TAG_DISCOUNT_BILL_ENTRY = "discount_bill_entry";
    private static String TAG_DISCOUNT_WITH_HOLD = "discount_with_hold";

    private static String TAG_TAX_PRODUCT = "tax_product";
    private static String TAG_TAX_BILL = "tax_bill";

    private static String EMPTY_PRODUCT_NAME = "empty_prod_name";
    private static String EMPTY_PRODUCT_QTY = "empty_prod_qty";
    private static String EMPTY_PRODUCT_PRICE = "empty_prod_price";
    private static String EMPTY_PRODUCT_LINE_VALUE = "empty_prod_line_value";

    private static String TAG_NET_PAYABLE = "net_amount";

    private static String TAG_NET_PAYABLE_IN_WORDS = "amount_word";

    private static String TAG_NET_SCHEME_DISCOUNT = "net_scheme_discount";

    private static String TAG_KEY1 = "key1";
    private static String TAG_KEY2 = "key2";

    private static String TAG_NET_PAYMENT_PAID_MODE = "bill_payment_paid";
    private static String TAG_NET_CREDIT = "bill_payment_balance";


    //Eod Report Fields
    private static String TAG_PRODUCT_LOADING_STOCK = "prod_lstock";
    private static String TAG_PRODUCT_SOLD_STOCK = "prod_sstock";
    private static String TAG_PRODUCT_FREE_ISSUED_STOCK = "prod_fstock";
    private static String TAG_PRODUCT_CURRENT_STOCK = "prod_cstock";
    private static String TAG_PRODUCT_EMPTY_BOTTLE_STOCK = "prod_estock";
    private static String TAG_PRODUCT_RETURN_STOCK = "prod_return_stock";
    private static String TAG_PRODUCT_REP_STOCK = "prod_replaced_stock";
    private static String TAG_PRODUCT_NON_SALABLE = "prod_nonsalable_stock";

    private int mLoadStockTotal;
    private int mSoldStockTotal;
    private int mFreeStockTotal;
    private int mCurrentStockTotal;
    private int mEmptyStockTotal;
    private int mReturnStockTotal;
    private int mRepStockTotal;
    private int mNonSalableTotal;
    //

    private HashMap<String, String> mKeyValues;

    private double totalPriceOffValue = 0;
    private ArrayList<ProductMasterBO> mOrderedProductList;

    private double total_line_value_incl_tax = 0;
    private double mBillLevelDiscountValue = 0;
    private double mEmptyTotalValue = 0;
    private double total_net_payable = 0;
    public int width_image = 100;
    public int height_image = 100;
    private double mSchemeValueByAmountType = 0;
    private double netSchemeAmount = 0;
    private double mCash = 0, mCheque = 0, mTotCredit = 0, mCreditNoteValue = 0;

    public boolean isFromInvoiceTransaction;


    private CommonPrintHelper(Context context) {
        this.context = context;
        this.bmodel = (BusinessModel) context;
        orderHelper = OrderHelper.getInstance(context);
    }

    public static CommonPrintHelper getInstance(Context context) {
        if (instance == null) {
            instance = new CommonPrintHelper(context);
        }
        return instance;
    }

    private StringBuilder mInvoiceData;

    public StringBuilder getInvoiceData() {
        return mInvoiceData;
    }

    public void setInvoiceData(StringBuilder mInvoiceData) {
        this.mInvoiceData = mInvoiceData;
    }

    public boolean isLogoEnabled, isSignatureEnabled;
    public String signatureName;

    private Vector<AttributeListBO> mAttributeList;

    private int firstColumnWidth;
    private int emptyFirstColumnWidth;
    private boolean isEOD_PC = false;
    private boolean isEOD_CS = false;
    private boolean isEOD_OU = false;


    /**
     * Read the tag from xml file and prepare print string from objects
     * if isFromAsset is true, then pass file name
     * if isFromAsset is true, then pass full path of file
     *
     * @param fileNameWithPath
     * @param isFromAsset
     */
    public void xmlRead(String fileNameWithPath, boolean isFromAsset, Vector<ProductMasterBO> productList, HashMap<String, String> keyValues, String signatureName, ArrayList<StockReportBO> eodStockList, ArrayList<String> vanUnLoadReasonList) {
        try {

            resetValues();

            mKeyValues = keyValues;
            this.signatureName = signatureName;

            InputStream xmlFile = null;
            StringBuilder sb = new StringBuilder();
            String newline = "\n";

            xmlFactoryObject = XmlPullParserFactory.newInstance();
            xmlParser = xmlFactoryObject.newPullParser();

            if (isFromAsset)
                xmlFile = context.getAssets().open(fileNameWithPath);
            else {
                File file = new File(getXmlFilePath(fileNameWithPath));
                xmlFile = new FileInputStream(file);
            }

            xmlParser.setInput(xmlFile, null);

            String group_name = "";
            String product_bacth = "", product_free_product = "";
            String scheme_discount = "";
            String wrap_text = "";
            mAttributeList = null;

            int property_total_length = 0;
            String property_special = "";
            String lineValue = "";

            String product_name_single_line = "";


            int product_header_border_char_length = 0;
            String product_header_border_char = "-";

            int mLengthUptoPName = 0;
            firstColumnWidth = 0;
            emptyFirstColumnWidth = 0;
            StringBuilder vanunloadHeader = new StringBuilder();

            int event = xmlParser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                String name = xmlParser.getName();

                switch (event) {
                    case XmlPullParser.START_TAG:
                        if (name.equals("line")) {
                            group_name = xmlParser.getAttributeValue(null, "group");
                            if (group_name != null) {
                                product_bacth = xmlParser.getAttributeValue(null, "batch");
                                product_bacth = product_bacth == null ? "no" : product_bacth;

                                product_free_product = xmlParser.getAttributeValue(null, "scheme_free_product");
                                product_free_product = product_free_product == null ? "no" : product_free_product;

                                scheme_discount = xmlParser.getAttributeValue(null, "scheme_discount");
                                scheme_discount = scheme_discount == null ? "no" : scheme_discount;

                                product_name_single_line = xmlParser.getAttributeValue(null, "prod_name_single_line");
                                product_name_single_line = product_name_single_line == null ? "no" : product_name_single_line;

                                String product_header_border_string = xmlParser.getAttributeValue(null, "length");
                                product_header_border_char_length = product_header_border_string == null ? property_total_length : SDUtil.convertToInt(product_header_border_string);

                                product_header_border_char = xmlParser.getAttributeValue(null, "text");
                                product_header_border_char = product_header_border_char == null ? "-" : product_header_border_char;

                                mAttributeList = new Vector<>();
                                if (group_name.equals("product_details")
                                        || group_name.equals("eod_product_details")
                                        || group_name.equalsIgnoreCase("van_unload_details")) {
                                    for (int i = 0; i < product_header_border_char_length; i++) {
                                        sb.append(product_header_border_char);
                                    }
                                    sb.append(newline);

                                    if (group_name.equals("van_unload_details")) {
                                        sb.append(centerAlignment(context.getString(R.string.salable), property_special, property_total_length, property_total_length));
                                        sb.append(newline);
                                    }

                                }

                            }

                            lineValue = "";
                        } else if (name.equals("view")) {
                            String attr_name = xmlParser.getAttributeValue(null, "name");
                            int attr_length = SDUtil.convertToInt(xmlParser.getAttributeValue(null, "length"));

                            String attr_text = xmlParser.getAttributeValue(null, "text");
                            attr_text = attr_text == null ? "" : attr_text;

                            String attr_secondary_text = xmlParser.getAttributeValue(null, "secondarytext");
                            attr_secondary_text = attr_secondary_text == null ? "" : attr_secondary_text;

                            String attr_padding = xmlParser.getAttributeValue(null, "padding");
                            attr_padding = attr_padding == null ? "left" : attr_padding;

                            String attr_space_str = xmlParser.getAttributeValue(null, "space");
                            int attr_space = attr_space_str == null ? 0 : SDUtil.convertToInt(attr_space_str);

                            String attr_precision_str = xmlParser.getAttributeValue(null, "precision_count");
                            int attr_precision = attr_precision_str == null ? -1 : SDUtil.convertToInt(attr_precision_str);

                            String attr_align = xmlParser.getAttributeValue(null, "align");
                            attr_align = attr_align == null ? "left" : attr_align;

                            String attr_bold = xmlParser.getAttributeValue(null, "bold");
                            attr_bold = attr_bold == null ? "No" : attr_bold;

                            String attr_repeat = xmlParser.getAttributeValue(null, "repeat");
                            attr_repeat = attr_repeat == null ? "No" : attr_repeat;

                            wrap_text = xmlParser.getAttributeValue(null, "wrap_text");
                            wrap_text = wrap_text == null ? "no" : wrap_text;

                            String mAttrValue = "";

                            if (attr_name.equals("label")) {
                                if (attr_repeat.equalsIgnoreCase("yes")) {
                                    for (int r = 0; r < attr_length; r++) {
                                        mAttrValue = mAttrValue + attr_text;
                                    }
                                } else {
                                    mAttrValue = attr_text;
                                }


                            } else {
                                mAttrValue = getValue(attr_name, attr_text, attr_secondary_text, attr_precision, product_name_single_line, attr_space_str);

                                if (mAttrValue.equals("-1")) {
                                    if (!attr_text.equals(""))
                                        mAttrValue = attr_text;
                                    else
                                        mAttrValue = attr_name;
                                }

                            }
                            if (!attr_name.contains("discount_bill_entry")
                                    && !attr_name.contains("discount_product")
                                    && !attr_name.contains("tax_product")
                                    && !attr_name.contains("tax_bill")
                                    && !attr_name.contains("empty_total")
                                    && !attr_name.contains("line_total")
                                    && !attr_name.contains("line_excl_tax_total")
                                    && !attr_name.contains("net_amount")
                                    && !attr_name.contains("net_scheme_discount")
                                    && !attr_name.contains("amount_word")
                                    && !attr_name.contains("discount_with_hold")) {
                                if (attr_align.equalsIgnoreCase(ALIGNMENT_LEFT)) {
                                    int length = 0;
                                    if (bmodel.configurationMasterHelper.IS_SHOW_PRINT_LANGUAGE_THAI && !attr_name.equalsIgnoreCase("label") && !attr_repeat.equalsIgnoreCase("yes"))
                                        length = getThaiFontLength(mAttrValue, (float) 5.8);
                                    else
                                        length = mAttrValue.length();

                                    if (length > attr_length) {
                                        if (!attr_name.equalsIgnoreCase(TAG_PRODUCT_NAME)
                                                || (attr_name.equalsIgnoreCase(TAG_PRODUCT_NAME) && product_name_single_line.equalsIgnoreCase("NO"))) {

                                            if (wrap_text.equalsIgnoreCase("no"))
                                                mAttrValue = mAttrValue.substring(0, attr_length - property_special.length()) + property_special;
                                        }
                                    } else if (length < attr_length) {
                                        int diff = attr_length - length;

                                        if (attr_padding.equalsIgnoreCase(ALIGNMENT_RIGHT)) {
                                            mAttrValue = doAlign(mAttrValue, ALIGNMENT_RIGHT, diff);
                                        } else {
                                            mAttrValue = doAlign(mAttrValue, ALIGNMENT_LEFT, diff);
                                        }

                                    }
                                    if (length > attr_length
                                            && wrap_text.equalsIgnoreCase("yes"))
                                        mAttrValue = doAlignWithNextLine(mAttrValue, attr_length, ALIGNMENT_RIGHT, attr_space, attr_bold);
                                    else
                                        mAttrValue = doAlign(mAttrValue, ALIGNMENT_RIGHT, attr_space);

                                } else if (attr_align.equalsIgnoreCase(ALIGNMENT_RIGHT)) {
                                    int startPosition;
                                    int length = mAttrValue.length();

                                    if (length > attr_length) {
                                        if (wrap_text.equalsIgnoreCase("no"))
                                            mAttrValue = mAttrValue.substring(0, attr_length - property_special.length()) + property_special;
                                    }

                                    if (attr_padding.equalsIgnoreCase(ALIGNMENT_RIGHT)) {
                                        startPosition = property_total_length - lineValue.length() - length;
                                    } else {
                                        startPosition = property_total_length - lineValue.length() - attr_length;
                                    }
                                    if (length > attr_length
                                            && wrap_text.equalsIgnoreCase("yes"))
                                        mAttrValue = doAlignWithNextLine(mAttrValue, attr_length, ALIGNMENT_RIGHT, attr_space, attr_bold);
                                    else
                                        mAttrValue = doAlign(mAttrValue, ALIGNMENT_RIGHT, startPosition);

                                } else if (attr_align.equalsIgnoreCase(ALIGNMENT_CENTER)) {
                                    int startPosition;

                                    int length = 0;
                                    if (bmodel.configurationMasterHelper.IS_SHOW_PRINT_LANGUAGE_THAI)
                                        length = getThaiFontLength(mAttrValue, (float) 5.8);
                                    else
                                        length = mAttrValue.length();

                                    if (length > attr_length) {
                                        if (wrap_text.equalsIgnoreCase("no"))
                                            mAttrValue = mAttrValue.substring(0, attr_length - property_special.length()) + property_special;
                                    }

                                    startPosition = (property_total_length / 2) - (length / 2);

                                    if (length > attr_length
                                            && wrap_text.equalsIgnoreCase("yes"))
                                        mAttrValue = doAlignWithNextLine(mAttrValue, attr_length, ALIGNMENT_RIGHT, startPosition, attr_bold);
                                    else
                                        mAttrValue = doAlign(mAttrValue, ALIGNMENT_RIGHT, startPosition);
                                }
                            }
                            if (attr_bold.equalsIgnoreCase("YES")) {
                                mAttrValue = "#B#" + mAttrValue;
                            }

                            sb.append(mAttrValue);

                            if (group_name != null
                                    && group_name.equalsIgnoreCase("van_unload_details"))
                                vanunloadHeader.append(mAttrValue);

                            lineValue += mAttrValue;


                            if (group_name != null && (group_name.equalsIgnoreCase("product_details")
                                    || group_name.equalsIgnoreCase("eod_product_details")
                                    || group_name.equals("van_unload_details"))) {
                                //mLengthUptoPName = mLengthUptoPName + attr_length + attr_space;
                                if (product_name_single_line.equalsIgnoreCase("YES")) {

                                    // If product name is single line, then second line should be printed after the first column of first line
                                    // So that bottom common labels will be aligned in straight to the first column(Ex:TAG_PRODUCT_LINE_TOTAL_WITH_QTY)..
                                    if (firstColumnWidth == 0) {
                                        firstColumnWidth = attr_length + attr_space;
                                    }
                                    if (attr_name.equalsIgnoreCase(TAG_PRODUCT_NAME)) {
                                        sb.append("\n");
                                        vanunloadHeader.append("\n");
                                        char emptySpace = ' ';
                                        for (int sp = 0; sp < firstColumnWidth; sp++) {
                                            sb.append(emptySpace);
                                            vanunloadHeader.append("\n");
                                        }

                                    }
                                }
                            }

                            if (group_name != null && group_name.equalsIgnoreCase("empty_return")) {
                                //mLengthUptoPName = mLengthUptoPName + attr_length + attr_space;
                                if (product_name_single_line.equalsIgnoreCase("YES")) {
                                    emptyFirstColumnWidth = 0;
                                    // If Empty product name is single line, then second line should be printed after the first column of first line
                                    // So that bottom common labels will be aligned in straight to the first column(Ex:TAG_PRODUCT_LINE_TOTAL_WITH_QTY)..

                                    if (emptyFirstColumnWidth == 0) {
                                        emptyFirstColumnWidth = attr_length + attr_space;
                                    }
                                    if (attr_name.equalsIgnoreCase(EMPTY_PRODUCT_NAME)) {
                                        sb.append("\n");
                                        char emptySpace = ' ';
                                        for (int sp = 0; sp < emptyFirstColumnWidth; sp++) {
                                            sb.append(emptySpace);
                                        }

                                    }
                                }
                            }


                            if (group_name != null) {
                                AttributeListBO attr = new AttributeListBO();
                                attr.setAttributeName(attr_name);
                                attr.setAttributeLength(attr_length);
                                attr.setAttributeText(attr_text);
                                attr.setAttributePadding(attr_padding);
                                attr.setAttributeSpace(attr_space);
                                attr.setAttributeSpecialChar(property_special);
                                attr.setmAttributePrecision(attr_precision);
                                mAttributeList.add(attr);
                            }
                        } else if (name.equals("property")) {
                            property_total_length = SDUtil.convertToInt(xmlParser.getAttributeValue(null, "total_length"));
                            mPaperLenghtInChar = property_total_length;
                            property_special = xmlParser.getAttributeValue(null, "special");
                            property_special = property_special == null ? "" : property_special;
                            String pres_str = xmlParser.getAttributeValue(null, "precision_count");
                            mGlobalPrecision = pres_str == null ? -1 : SDUtil.convertToInt(pres_str);
                        } else if (name.equalsIgnoreCase("logo")) {
                            isLogoEnabled = true;
                        } else if (name.equalsIgnoreCase("signature")) {
                            isSignatureEnabled = true;
                        } else if (name.equalsIgnoreCase("newline")) {
                            String attr_count_str = xmlParser.getAttributeValue(null, "count");
                            int attr_count = attr_count_str == null ? 1 : SDUtil.convertToInt(attr_count_str);
                            for (int n = 0; n < attr_count; n++) {
                                sb.append(newline);
                            }
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        if (name.equals("line")) {
                            if (group_name != null && group_name.equalsIgnoreCase("product_details")) {
                                sb.append(newline);
                                for (int i = 0; i < product_header_border_char_length; i++) {
                                    sb.append(product_header_border_char);
                                }

                                if (product_bacth.equalsIgnoreCase("YES"))
                                    loadProductsWithBatch(mAttributeList, sb, product_free_product, productList, product_name_single_line);
                                else
                                    loadProducts(mAttributeList, sb, product_free_product, productList, product_name_single_line);

                                if (scheme_discount.equalsIgnoreCase("YES"))
                                    loadSchemeDiscount(mAttributeList, product_name_single_line, sb);
                                else
                                    calculateSchemeAmountDiscountValue();

                                //Bill discount
                                double billDiscountValue = bmodel.getOrderHeaderBO() != null ? bmodel.getOrderHeaderBO().getBillLevelDiscountValue() : 0;
                                mBillLevelDiscountValue = billDiscountValue;
                                //

                                //Bill tax
                                double billLevelTax = bmodel.getOrderHeaderBO() != null ? bmodel.getOrderHeaderBO().getBillLevelTaxValue() : 0;

                                getEmptyReturnValue();
                                if (bmodel.configurationMasterHelper.SHOW_COLLECTION_BEFORE_INVOICE)
                                    getCollectionBeforeInvValue();


                                total_net_payable = (total_line_value_incl_tax + billLevelTax + mEmptyTotalValue) - mBillLevelDiscountValue;
                                mTotCredit = total_net_payable - (mCash + mCheque + mCreditNoteValue);
                            } else if (group_name != null && group_name.equalsIgnoreCase("empty_return")) {
                                sb.append(newline);
                                for (int i = 0; i < product_header_border_char_length; i++) {
                                    sb.append(product_header_border_char);
                                }

                                printEmptyReturn(mAttributeList, product_name_single_line, sb);
                            } else if (group_name != null && group_name.equalsIgnoreCase("eod_product_details")) {
                                isEOD_CS = bmodel.configurationMasterHelper.SHOW_EOD_OC;
                                isEOD_PC = bmodel.configurationMasterHelper.SHOW_EOD_OP;
                                isEOD_OU = bmodel.configurationMasterHelper.SHOW_EOD_OO;

                                if (bmodel.configurationMasterHelper.IS_EOD_STOCK_SPLIT) {
                                    sb.append(newline);
                                    addEODSubtitle(sb, property_special, property_total_length);
                                }
                                sb.append(newline);
                                for (int i = 0; i < product_header_border_char_length; i++) {
                                    sb.append(product_header_border_char);
                                }
                                if (eodStockList != null)
                                    loadEODStock(mAttributeList, sb, eodStockList, product_name_single_line, bmodel.configurationMasterHelper.IS_EOD_STOCK_SPLIT);
                            } else if (group_name != null
                                    && group_name.equalsIgnoreCase("van_unload_details")) {

                                sb.append(newline);
                                vanunloadHeader.append(newline);
                                for (int i = 0; i < product_header_border_char_length; i++) {
                                    sb.append(product_header_border_char);
                                    vanunloadHeader.append(product_header_border_char);

                                }

                                if (vanUnLoadReasonList != null)
                                    loadVanUnloadProducts(mAttributeList, sb, vanUnLoadReasonList, product_name_single_line, newline, property_special, property_total_length, vanunloadHeader, product_header_border_char_length, product_header_border_char);
                            }

                            group_name = "";
                            product_bacth = "NO";
                            product_free_product = "NO";
                            if (!lineValue.trim().equals(""))
                                sb.append(newline);
                            else
                                sb.replace(sb.lastIndexOf(newline) + 1, sb.length(), "");
                        }
                        break;
                }
                event = xmlParser.next();
            }
            setInvoiceData(sb);
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private int getThaiFontLength(String mAttrValue, float value) {
        TextPaint paint = new TextPaint();
        float width = paint.measureText(mAttrValue);
        return Math.round(width / value);
    }

    /**
     * Method used to get xml file from sdcard
     *
     * @param fileName
     * @return
     */
    public String getXmlFilePath(final String fileName) {

        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/"
                + bmodel.userMasterHelper.getUserMasterBO().getUserid()
                + "PRINT");

        File[] files = file.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                if (pathname.isDirectory()) {
                    return false;
                }

                String name = pathname.getName();
                int lastIndex = name.lastIndexOf('.');
                boolean isFileAvilable = name.startsWith(fileName);

                if (lastIndex < 0 && !isFileAvilable) {
                    return false;
                }
                return name.substring(lastIndex).equalsIgnoreCase(".xml") && isFileAvilable;
            }
        });

        if (files != null && files.length > 0) {
            return files[0].getAbsolutePath();
        }

        return "";
    }

    /**
     * Get the value from objects for the tag
     *
     * @param tag
     * @param label - will be append with value
     * @return
     */
    private String getValue(String tag, String label, String mSecondaryLabel, int precisionCount, String product_name_single_line, String attr_space_str) {
        String value = "-1";
        if (tag.equalsIgnoreCase(TAG_TITLE)) {
            if (bmodel.getRetailerMasterBO().getRfield2() != null
                    && !"".equals(bmodel.getRetailerMasterBO().getRfield2())
                    && bmodel.getRetailerMasterBO().getRfield2().equalsIgnoreCase("1")) {
                value = mSecondaryLabel;
            } else {
                value = label;
            }
        } else if (tag.equalsIgnoreCase(TAG_DATE)) {
            value = label + DateTimeUtils.convertFromServerDateToRequestedFormat(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                    bmodel.configurationMasterHelper.outDateFormat);
        } else if (tag.equalsIgnoreCase(TAG_TIME)) {
            value = label + DateTimeUtils.now(DateTimeUtils.TIME);
        } else if (tag.equalsIgnoreCase(TAG_DELIVERY_DATE)) {
            String deliveryDate = bmodel.getDeliveryDate(OrderHelper.getInstance(context).selectedOrderId, bmodel.getRetailerMasterBO().getRetailerID());
            if (!deliveryDate.equals("")) {
                String delDate = DateTimeUtils.convertFromServerDateToRequestedFormat(deliveryDate, bmodel.configurationMasterHelper.outDateFormat);
                value = label + delDate;
            }
        } else if (tag.equalsIgnoreCase(TAG_INVOICE_NUMBER)) {
            value = label + bmodel.invoiceNumber;
        } else if (tag.equalsIgnoreCase(TAG_DISTRIBUTOR_NAME)) {
            value = label + bmodel.userMasterHelper.getUserMasterBO()
                    .getDistributorName();
        } else if (tag.equalsIgnoreCase(TAG_DISTRIBUTOR_ADDRESS1)) {
            value = label + bmodel.userMasterHelper.getUserMasterBO()
                    .getDistributorAddress1();
        } else if (tag.equalsIgnoreCase(TAG_DISTRIBUTOR_ADDRESS2)) {
            value = label + bmodel.userMasterHelper.getUserMasterBO()
                    .getDistributorAddress2();
        } else if (tag.equalsIgnoreCase(TAG_DISTRIBUTOR_ADDRESS3)) {
            value = label + bmodel.userMasterHelper.getUserMasterBO()
                    .getDistributorAddress3();
        } else if (tag.equalsIgnoreCase(TAG_DISTRIBUTOR_CONTACT_NUMBER)) {
            if (bmodel.userMasterHelper.getUserMasterBO()
                    .getDistributorContactNumber() != null && !bmodel.userMasterHelper.getUserMasterBO()
                    .getDistributorContactNumber().equalsIgnoreCase("null") && !bmodel.userMasterHelper.getUserMasterBO()
                    .getDistributorContactNumber().equalsIgnoreCase(""))
                value = label + bmodel.userMasterHelper.getUserMasterBO()
                        .getDistributorContactNumber();
            else
                value = label + "----";
        } else if (tag.equalsIgnoreCase(TAG_DISTRIBUTOR_TIN_NUMBER)) {
            value = label + bmodel.userMasterHelper.getUserMasterBO()
                    .getDistributorTinNumber();
        } else if (tag.equalsIgnoreCase(TAG_DISTRIBUTOR_CST_NUMBER)) {
            value = label + bmodel.userMasterHelper.getUserMasterBO()
                    .getCstNo();
        } else if (tag.equalsIgnoreCase(TAG_DISTRIBUTOR_FAX_NUMBER)) {
            if (bmodel.userMasterHelper.getUserMasterBO()
                    .getFaxNo() != null && !bmodel.userMasterHelper.getUserMasterBO()
                    .getFaxNo().equalsIgnoreCase("null") && !bmodel.userMasterHelper.getUserMasterBO()
                    .getFaxNo().equalsIgnoreCase(""))
                value = label + bmodel.userMasterHelper.getUserMasterBO()
                        .getFaxNo();
            else
                value = label + "----";
        } else if (tag.equalsIgnoreCase(TAG_DISTRIBUTOR_GST_NUMBER)) {
            value = label + bmodel.userMasterHelper.getUserMasterBO()
                    .getGSTNumber();
        } else if (tag.equalsIgnoreCase(TAG_SUPPLIER_NAME)) {
            if (bmodel.getRetailerMasterBO().getSupplierBO() != null)
                value = label + bmodel.getRetailerMasterBO().getSupplierBO()
                        .getSupplierName();
        } else if (tag.equalsIgnoreCase(TAG_RETAILER_NAME)) {
            value = label + bmodel.getRetailerMasterBO().getRetailerName();
        } else if (tag.equalsIgnoreCase(TAG_RETAILER_CODE)) {
            value = label + bmodel.getRetailerMasterBO().getRetailerCode();
        } else if (tag.equalsIgnoreCase(TAG_BEAT_CODE)) {
            value = label + bmodel.beatMasterHealper.getBeatMasterBOByID(bmodel.getRetailerMasterBO().getBeatID()).getBeatCode();
        } else if (tag.equalsIgnoreCase(TAG_RETAILER_ADDRESS1)) {
            value = label + bmodel.getRetailerMasterBO().getAddress1();
        } else if (tag.equalsIgnoreCase(TAG_RETAILER_ADDRESS2)) {
            value = label + bmodel.getRetailerMasterBO().getAddress2();
        } else if (tag.equalsIgnoreCase(TAG_RETAILER_ADDRESS3)) {
            value = label + bmodel.getRetailerMasterBO().getAddress3();
        } else if (tag.equalsIgnoreCase(TAG_RETAILER_CITY)) {
            value = label + bmodel.getRetailerMasterBO().getCity();
        } else if (tag.equalsIgnoreCase(TAG_RETAILER_PIN_CODE)) {
            value = label + bmodel.getRetailerMasterBO().getPincode();
        } else if (tag.equalsIgnoreCase(TAG_RETAILER_CONTACT_NUMBER)) {
            value = label + bmodel.getRetailerMasterBO().getContactnumber();
        } else if (tag.equalsIgnoreCase(TAG_RETAILER_TIN_NUMBER)) {
            value = label + bmodel.getRetailerMasterBO().getTinnumber();
        } else if (tag.equalsIgnoreCase(TAG_RETAILER_CST_NUMBER)) {
            value = label + bmodel.getRetailerMasterBO().getCredit_invoice_count();
        } else if (tag.equalsIgnoreCase(TAG_RETAILER_GST_NUMBER)) {
            value = label + bmodel.getRetailerMasterBO().getGSTNumber();
        } else if (tag.equalsIgnoreCase(TAG_SELLER_CODE)) {
            value = label + bmodel.userMasterHelper.getUserMasterBO().getUserCode();
        } else if (tag.equalsIgnoreCase(TAG_SELLER_NAME)) {
            value = label + bmodel.userMasterHelper.getUserMasterBO().getUserName();
        } else if (tag.equalsIgnoreCase(TAG_SELLER_ID)) {
            value = label + bmodel.userMasterHelper.getUserMasterBO().getUserid();
        } else if (tag.equalsIgnoreCase(TAG_DISCOUNT_PRODUCT_PRICE_OFF)) {
            value = alignWithLabelForSingleLine(label, formatValueInPrint(totalPriceOffValue, precisionCount));
        } else if (tag.equalsIgnoreCase(TAG_DISCOUNT_PRODUCT_APPLY)) {
            value = getProductLevelApplyDiscount(label, precisionCount);
        } else if (tag.equalsIgnoreCase(TAG_DISCOUNT_PRODUCT_ENTRY)) {
            value = alignWithLabelForSingleLine(label, formatValueInPrint((DiscountHelper.getInstance(context).calculateUserEntryLevelDiscount(mOrderedProductList)), precisionCount));
        } else if (tag.equalsIgnoreCase(TAG_TAX_PRODUCT)) {
            value = getProductLevelTax(label, precisionCount);
        } else if (tag.equalsIgnoreCase(TAG_DISCOUNT_BILL_ENTRY)) {
            int extraSpace = 0;
            extraSpace = SDUtil.convertToInt(attr_space_str);
            value = alignWithLabelForSingleLine(label, formatValueInPrint(mBillLevelDiscountValue, precisionCount) + "", extraSpace);
        } else if (tag.equalsIgnoreCase(TAG_TAX_BILL)) {
            value = printBillLevelTax(precisionCount, attr_space_str);
        } else if (tag.equalsIgnoreCase(TAG_PRODUCT_LINE_TOTAL)) {
            int extraSpace = 0;
            extraSpace = SDUtil.convertToInt(attr_space_str);
            value = alignWithLabelForSingleLine(label, formatValueInPrint(total_line_value_incl_tax, precisionCount), extraSpace);
        } else if (tag.equalsIgnoreCase(TAG_PRODUCT_LINE_EXCLUDING_TAX_TOTAL)) {
            int extraSpace = 0;
            extraSpace = SDUtil.convertToInt(attr_space_str);
            value = alignWithLabelForSingleLine(label, formatSalesValueInPrint(mProductLineValueExcludingTaxTotal, precisionCount), extraSpace);
        } else if (tag.equalsIgnoreCase(TAG_PRODUCT_LINE_TOTAL_WITH_QTY)) {
            value = getTotalWithQty(label, product_name_single_line);
        } else if (tag.equalsIgnoreCase(TAG_NET_PAYABLE)) {
            int extraSpace = 0;
            extraSpace = SDUtil.convertToInt(attr_space_str);
            value = alignWithLabelForSingleLine(label, formatSalesValueInPrint(total_net_payable, precisionCount), extraSpace);
        } else if (tag.equalsIgnoreCase(TAG_NET_PAYABLE_IN_WORDS)) {
            value = label + getAmountInWords(formatSalesValueInPrint(total_net_payable, precisionCount).replace(",", ""));
        } else if (tag.equalsIgnoreCase("empty_total")) {
            value = alignWithLabelForSingleLine(label, formatValueInPrint(mEmptyTotalValue, precisionCount));
        } else if (tag.equalsIgnoreCase(TAG_KEY1)) {
            if (mKeyValues != null)
                value = label + mKeyValues.get(TAG_KEY1);
        } else if (tag.equalsIgnoreCase(TAG_KEY2)) {
            if (mKeyValues != null)
                value = label + mKeyValues.get(TAG_KEY2);
        } else if (tag.equalsIgnoreCase(TAG_ORDER_NUMBER)) {
            value = label + orderHelper.getOrderId().replaceAll("\'", "");
        } else if (tag.equalsIgnoreCase(TAG_RETAILER_ROUTE)) {
            value = label + bmodel.beatMasterHealper.getBeatMasterBOByID(bmodel.getRetailerMasterBO().getBeatID());
        } else if (tag.equalsIgnoreCase(TAG_NET_SCHEME_DISCOUNT)) {
            int extraSpace = 0;
            extraSpace = SDUtil.convertToInt(attr_space_str);
            value = alignWithLabelForSingleLine(label, formatValueInPrint(netSchemeAmount, precisionCount), extraSpace);
        } else if (tag.equalsIgnoreCase(TAG_PRODUCT_LINE_TOTAL_QTY)) {
            value = getProductTotalQty(label);
        } else if (tag.equalsIgnoreCase(TAG_NET_PAYMENT_PAID_MODE)) {
            value = getCollectionAmount(label, precisionCount);
        } else if (tag.equalsIgnoreCase(TAG_NET_CREDIT)) {
            value = alignWithLabelForSingleLine(label, formatValueInPrint(mTotCredit, precisionCount));
        }


        return value;
    }

    private String getTotalWithQty(String label, String product_name_single_line) {
        String mProductValue;
        StringBuilder sb = new StringBuilder();

        boolean isLabelPrinted = false;// to check label is printed or not..

        for (AttributeListBO attr : mAttributeList) {
            mProductValue = "";
            if (!isLabelPrinted && attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_CODE)) {
                mProductValue = label;
                isLabelPrinted = true;
            } else if (!isLabelPrinted && attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_NAME)) {
                mProductValue = label;
                isLabelPrinted = true;
            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_QTY_CASE)) {
                mProductValue = mProductCaseQtyTotal + "";
            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_QTY_PIECE)) {
                mProductValue = mProductPieceQtyTotal + "";
            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_QTY_OUTER)) {
                mProductValue = mProductOuterQtyTotal + "";
            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_QTY_TOTAL_IN_PIECE)) {
                mProductValue = mProductQtyInPieceTotal + "";
            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_LINE_VALUE)) {
                mProductValue = formatValueInPrint(mProductLineValueTotal, attr.getmAttributePrecision());
            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_LINE_VALUE_EXCLUDING_TAX)) {
                mProductValue = formatValueInPrint(mProductLineValueExcludingTaxTotal, attr.getmAttributePrecision());
            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_lINE_VALUE_INCLUDING_TAX)) {
                mProductValue = formatValueInPrint(mProductLineValueIncludingTaxTotal, attr.getmAttributePrecision());
            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_REPLACE_QTY_PIECE)) {
                mProductValue = mProductRepQtyTotal + "";
            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_RETURN_QTY_PIECE)) {
                mProductValue = mProductRetQtyTotal + "";
            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_SUM_QTY_PIECE_WITH_REP)) {
                mProductValue = mProductRepOrdInPieceTotal + "";
            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_LOADING_STOCK)) {
                mProductValue = mLoadStockTotal + "";
            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_SOLD_STOCK)) {
                mProductValue = mSoldStockTotal + "";
            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_FREE_ISSUED_STOCK)) {
                mProductValue = mFreeStockTotal + "";
            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_CURRENT_STOCK)) {
                mProductValue = mCurrentStockTotal + "";
            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_EMPTY_BOTTLE_STOCK)) {
                mProductValue = mEmptyStockTotal + "";
            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_RETURN_STOCK)) {
                mProductValue = mReturnStockTotal + "";
            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_REP_STOCK)) {
                mProductValue = mRepStockTotal + "";
            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_NON_SALABLE)) {
                mProductValue = mNonSalableTotal + "";
            }


            if (!product_name_single_line.equalsIgnoreCase("YES")
                    || (!attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_CODE) && !attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_NAME))) {

                if (mProductValue.length() > attr.getAttributeLength()) {
                    mProductValue = mProductValue.substring(0, attr.getAttributeLength() - attr.getAttributeSpecialChar().length()) + attr.getAttributeSpecialChar();
                } else if (mProductValue.length() < attr.getAttributeLength()) {
                    int diff = attr.getAttributeLength() - mProductValue.length();

                    if (attr.getAttributePadding().equalsIgnoreCase(ALIGNMENT_RIGHT)) {
                        mProductValue = doAlign(mProductValue, ALIGNMENT_RIGHT, diff);
                    } else {
                        mProductValue = doAlign(mProductValue, ALIGNMENT_LEFT, diff);
                    }
                }

            } else if (product_name_single_line.equalsIgnoreCase("YES")
                    && (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_CODE) || attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_NAME))) {
                // To take first column width exactly
                if (mProductValue.length() > attr.getAttributeLength()) {
                    mProductValue = mProductValue.substring(0, attr.getAttributeLength() - attr.getAttributeSpecialChar().length()) + attr.getAttributeSpecialChar();
                } else {
                    int diff = attr.getAttributeLength() - mProductValue.length();

                    if (attr.getAttributePadding().equalsIgnoreCase(ALIGNMENT_RIGHT)) {
                        mProductValue = doAlign(mProductValue, ALIGNMENT_RIGHT, diff);
                    } else {
                        mProductValue = doAlign(mProductValue, ALIGNMENT_LEFT, diff);
                    }
                }
            }

            mProductValue = doAlign(mProductValue, ALIGNMENT_RIGHT, attr.getAttributeSpace());

            sb.append(mProductValue);
        }
        return sb.toString();
    }

    private String getProductTotalQty(String label) {
        StringBuilder sb = new StringBuilder();
        String mProductTotalQty;
        boolean isLabelPrinted = false;

        for (int index = 0; index < mAttributeList.size(); index++) {

            AttributeListBO attributeBO = mAttributeList.get(index);
            mProductTotalQty = "";
            if (!isLabelPrinted) {
                mProductTotalQty = label;
                isLabelPrinted = true;
            } else if (attributeBO.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_QTY_CASE)) {
                mProductTotalQty = mProductCaseQtyTotal + " " + attributeBO.getAttributeText() + " / ";
            } else if (attributeBO.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_QTY_PIECE)) {
                mProductTotalQty = mProductPieceQtyTotal + " " + attributeBO.getAttributeText() + " / ";
            } else if (attributeBO.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_QTY_OUTER)) {
                mProductTotalQty = mProductOuterQtyTotal + " " + attributeBO.getAttributeText() + " / ";
            }

            mProductTotalQty = doAlign(mProductTotalQty, ALIGNMENT_LEFT, attributeBO.getAttributeSpace());
            sb.append(mProductTotalQty);


        }

        String value = sb.toString().trim().substring(0, sb.toString().trim().length() - 1);

        return value;
    }


    /**
     * @param mAttributeList
     * @param sb
     * @param vanUnLoadReasonList
     * @param product_name_single_line
     * @param newline
     * @param property_special
     * @param property_total_length
     */

    private void loadVanUnloadProducts(Vector<AttributeListBO> mAttributeList, StringBuilder sb, ArrayList<String> vanUnLoadReasonList, String product_name_single_line, String newline, String property_special, int property_total_length, StringBuilder vanunloadHeader, int product_header_border_char_length, String product_header_border_char) {
        try {
            ArrayList<String> bottomLineValues = new ArrayList<>();
            VanUnLoadModuleHelper vanUnLoadModuleHelper = VanUnLoadModuleHelper.getInstance(context);


            for (String reasonBo : vanUnLoadReasonList) {

                //print Reason Name
                if (!reasonBo.equalsIgnoreCase(context.getString(R.string.salable))) {
                    sb.append(newline);
                    sb.append(centerAlignment(reasonBo, property_special, property_total_length, property_total_length));
                    sb.append(newline);
                    sb.append(vanunloadHeader.toString());

                }

                sb.append("\n");
                String mProductValue = "";
                int mLengthUptoPName;
                mProductQtyInPieceTotal = 0;
                mProductLineValueTotal = 0;
                for (LoadManagementBO unLoadBo : vanUnLoadModuleHelper.getVanUnLoadListHashMap().get(reasonBo)) {

                    if (unLoadBo.getOrderedPcsQty() > 0 || unLoadBo.getOrderedCaseQty() > 0
                            || unLoadBo.getOuterOrderedCaseQty() > 0) {
                        mLengthUptoPName = 0;
                        for (AttributeListBO attr : mAttributeList) {
                            if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_NAME)) {
                                mProductValue = (unLoadBo.getProductname());
                            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_QTY_TOTAL_IN_PIECE)) {
                                mProductValue = (unLoadBo.getOrderedPcsQty()
                                        + (unLoadBo.getOrderedCaseQty() * unLoadBo.getCaseSize())
                                        + (unLoadBo.getOuterOrderedCaseQty() * unLoadBo.getOuterSize())) + "";
                                mProductQtyInPieceTotal = mProductQtyInPieceTotal + Integer.parseInt(mProductValue);
                            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_MRP)) {
                                mProductValue = formatValueInPrint(unLoadBo.getBaseprice(), attr.getmAttributePrecision());
                            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_LINE_VALUE)) {
                                double lineTotal = (unLoadBo.getOuterOrderedCaseQty() * unLoadBo.getBaseprice())
                                        + (unLoadBo.getOrderedCaseQty() * unLoadBo.getBaseprice())
                                        + (unLoadBo.getOrderedPcsQty() * unLoadBo.getBaseprice());
                                mProductValue = formatValueInPrint(lineTotal, attr.getmAttributePrecision());
                                mProductLineValueTotal = mProductLineValueTotal + Double.parseDouble(mProductValue.replace(",", ""));
                            }


                            if (!attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_NAME) || product_name_single_line.equalsIgnoreCase("NO")) {
                                if (mProductValue.length() > attr.getAttributeLength()) {
                                    mProductValue = mProductValue.substring(0, attr.getAttributeLength() - attr.getAttributeSpecialChar().length()) + attr.getAttributeSpecialChar();
                                } else if (mProductValue.length() < attr.getAttributeLength()) {
                                    int diff = attr.getAttributeLength() - mProductValue.length();

                                    if (attr.getAttributePadding().equalsIgnoreCase(ALIGNMENT_RIGHT)) {
                                        mProductValue = doAlign(mProductValue, ALIGNMENT_RIGHT, diff);
                                    } else {
                                        mProductValue = doAlign(mProductValue, ALIGNMENT_LEFT, diff);
                                    }
                                }
                            }
                            mProductValue = doAlign(mProductValue, ALIGNMENT_RIGHT, attr.getAttributeSpace());

                            sb.append(mProductValue);


                            //print the text which is next to the product name into next line
                            if (product_name_single_line.equalsIgnoreCase("YES")) {
                                // If product name is single line, then second line should be printed after the first column of first line
                                // So that bottom common labels will be aligned in straight to the first column(Ex:TAG_PRODUCT_LINE_TOTAL_WITH_QTY)..
                                mLengthUptoPName = mLengthUptoPName + attr.getAttributeLength() + attr.getAttributeSpace();
                                if (firstColumnWidth == 0) {
                                    firstColumnWidth = attr.getAttributeLength() + attr.getAttributeSpace();
                                }
                                if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_NAME)) {
                                    sb.append("\n");
                                    char emptySpace = ' ';
                                    for (int sp = 0; sp < firstColumnWidth; sp++) {
                                        sb.append(emptySpace);
                                    }
                                }
                            }

                        }
                        sb.append("\n");
                    }
                }

                //add sum of total value by reason wise
                // bottomLineValue.put(entry.getKey(), mProductLineValueTotal);
                bottomLineValues.add(reasonBo + "-" + String.valueOf(mProductLineValueTotal));

                sb.append(newline);
                addProductHeaderBorder(sb, product_header_border_char_length, product_header_border_char);
                sb.append(newline);
                //Print total line with qty
                String mAttrValue = getTotalWithQty(context.getString(R.string.total) + ":", product_name_single_line);
                leftAlignment(context.getString(R.string.total), mAttrValue, property_special, property_total_length, 38, product_name_single_line, "right");
                mAttrValue = doAlign(mAttrValue, ALIGNMENT_RIGHT, 0);
                sb.append(mAttrValue);

                sb.append(newline);
                addProductHeaderBorder(sb, product_header_border_char_length, product_header_border_char);


            }
            // Print reason wise sum of total value
            sb.append(newline);
            sb.append(getSumofReasonWiseLineValue(bottomLineValues, -1, property_special, property_total_length, product_name_single_line));
            sb.append("  \n");
            sb.append("  \n");
            sb.append("  \n");
            sb.append("  \n");
        } catch (Exception e) {

        }
    }

    /**
     * get sum of total value based on reason wise
     *
     * @param bottomLineValues
     * @param precisionCount
     * @param property_special
     * @param property_total_length
     * @param product_name_single_line
     * @return - total value
     */
    private String getSumofReasonWiseLineValue(ArrayList<String> bottomLineValues, int precisionCount, String property_special, int property_total_length, String product_name_single_line) {
        StringBuffer sb = new StringBuffer();
        String mode = "";
        String s = ""; // load sum of reason wise total value

        for (String totalEntry : bottomLineValues) {
            mode = context.getString(R.string.total) + " ";
            s = alignWithLabelForSingleLine((mode + totalEntry.split("-")[0] + ":"), formatValueInPrint(SDUtil.convertToDouble(totalEntry.split("-")[1]), precisionCount));
            if (!s.isEmpty()) {
                leftAlignment((context.getString(R.string.total) + totalEntry.split("-")[0] + ":"), s, property_special, property_total_length, 38, product_name_single_line, "right");
                s = doAlign(s, ALIGNMENT_RIGHT, 0);
                sb.append(s);
            }

            sb.append("\n");
        }
        sb.append("  \n");
        return sb.toString();

    }


    /**
     * load the ordered products value from product object
     *
     * @param mAttrList
     * @param sb
     * @param isLoadSchemeFreeProduct - indicate to load scheme product
     */
    private void loadProducts(Vector<AttributeListBO> mAttrList, StringBuilder sb, String isLoadSchemeFreeProduct, Vector<ProductMasterBO> productList, String product_name_single_line) {

        //Get the ordered product from product list
        //Vector<ProductMasterBO> productList = bmodel.productHelper.getProductMaster();
        int productsCount = productList.size();
        mOrderedProductList = new ArrayList<ProductMasterBO>();
        firstColumnWidth = 0;

        ProductMasterBO productBO;
        for (int i = 0; i < productsCount; i++) {
            productBO = productList.elementAt(i);
            if (productBO.getTotalOrderedQtyInPieces() > 0
                    || ((bmodel.configurationMasterHelper.SHOW_SALES_RETURN_IN_DELIVERY || bmodel.configurationMasterHelper.SHOW_SALES_RETURN_IN_ORDER)
                    && isReturnDoneForProduct(productBO))) {
                mOrderedProductList.add(productBO);
            }

        }


        sb.append("\n");
        String mProductValue;
        int mLengthUptoPName;

        mProductCaseQtyTotal = 0;
        mProductPieceQtyTotal = 0;
        mProductOuterQtyTotal = 0;
        mProductQtyInPieceTotal = 0;

        mProductLineValueTotal = 0;
        mProductLineValueExcludingTaxTotal = 0;
        mProductLineValueIncludingTaxTotal = 0;

        mProductRetQtyTotal = 0;
        mProductRepQtyTotal = 0;
        mProductRepOrdInPieceTotal = 0;

        for (ProductMasterBO prod : mOrderedProductList) {
            mLengthUptoPName = 0;

            //load the ordered product line item - start
            for (AttributeListBO attr : mAttrList) {

                mProductValue = "";
                if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_CODE)) {
                    mProductValue = prod.getProductCode();
                } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_NAME)) {
                    mProductValue = (prod.getProductShortName() != null ? prod.getProductShortName() : prod.getProductName());
                } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_PRICE_CASE)) {
                    mProductValue = formatValueInPrint(prod.getCsrp(), attr.getmAttributePrecision());
                } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_PRICE_PIECE)) {
                    mProductValue = formatValueInPrint(prod.getSrp(), attr.getmAttributePrecision());
                } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_MRP)) {
                    mProductValue = formatValueInPrint(prod.getMRP(), attr.getmAttributePrecision());
                } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_PRICE_OUTER)) {
                    mProductValue = formatValueInPrint(prod.getOsrp(), attr.getmAttributePrecision());
                } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_QTY_CASE)) {
                    mProductValue = prod.getOrderedCaseQty() + "";
                    mProductCaseQtyTotal = mProductCaseQtyTotal + prod.getOrderedCaseQty();
                } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_QTY_PIECE)) {
                    mProductValue = prod.getOrderedPcsQty() + "";
                    mProductPieceQtyTotal = mProductPieceQtyTotal + prod.getOrderedPcsQty();
                } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_QTY_OUTER)) {
                    mProductValue = prod.getOrderedOuterQty() + "";
                    mProductOuterQtyTotal = mProductOuterQtyTotal + prod.getOrderedOuterQty();
                } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_UOM_WISE_QTY)) {
                    mProductValue = getUomWiseQty(prod);
                } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_QTY_TOTAL_IN_PIECE)) {
                    mProductValue = prod.getTotalOrderedQtyInPieces() + "";
                    mProductQtyInPieceTotal = mProductQtyInPieceTotal + SDUtil.convertToInt(mProductValue);
                } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_LINE_VALUE)) {
                    mProductValue = formatValueInPrint(prod.getLineValue(), attr.getmAttributePrecision());
                    mProductLineValueTotal = mProductLineValueTotal + SDUtil.convertToDouble(mProductValue.replace(",", ""));
                } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_LINE_VALUE_EXCLUDING_TAX)) {
                    mProductValue = formatValueInPrint(prod.getTaxableAmount() > 0 ? prod.getTaxableAmount() : prod.getNetValue(), attr.getmAttributePrecision());
                    mProductLineValueExcludingTaxTotal = mProductLineValueExcludingTaxTotal + (prod.getTaxableAmount() > 0 ? prod.getTaxableAmount() : prod.getNetValue());
                } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_lINE_VALUE_INCLUDING_TAX)) {
                    mProductValue = formatValueInPrint(prod.getNetValue(), attr.getmAttributePrecision());
                    mProductLineValueIncludingTaxTotal = mProductLineValueIncludingTaxTotal + prod.getNetValue();
                } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_REPLACE_QTY_PIECE)) {
                    mProductValue = prod.getRepPieceQty() + (prod.getRepCaseQty() * prod.getCaseSize()) + (prod.getRepOuterQty() * prod.getOutersize()) + "";
                    mProductRepQtyTotal = mProductRepQtyTotal + Integer.parseInt(mProductValue.replace(",", ""));
                } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_RETURN_QTY_PIECE)) {
                    int lineValue = 0;
                    if (prod.getSalesReturnReasonList().size() > 0) {
                        for (SalesReturnReasonBO obj : prod.getSalesReturnReasonList())
                            lineValue = lineValue + obj.getPieceQty() + (obj.getCaseQty() * obj.getCaseSize()) + (obj.getOuterQty() * obj.getOuterSize());
                    }
                    mProductValue = lineValue + "";
                    mProductRetQtyTotal = mProductRetQtyTotal + Integer.parseInt(mProductValue.replace(",", ""));
                } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_SUM_QTY_PIECE_WITH_REP)) {
                    mProductValue = prod.getRepPieceQty()
                            + (prod.getRepCaseQty() * prod.getCaseSize())
                            + (prod.getRepOuterQty() * prod.getOutersize())
                            + (prod.getTotalOrderedQtyInPieces()) + "";
                    mProductRepOrdInPieceTotal = mProductRepOrdInPieceTotal + Integer.parseInt(mProductValue.replace(",", ""));
                } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_TAG_DESC)) {
                    mProductValue = prod.getDescription() + "";
                } else if (attr.getAttributeName().equalsIgnoreCase(TAG_HSN_CODE)) {
                    mProductValue = prod.getProductCode();
                } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_TAX_PERCENTAGE)) {
                    double taxPercentage = 0;
                    if (bmodel.productHelper.taxHelper.getmTaxListByProductId() != null) {
                        ArrayList<TaxBO> taxList = bmodel.productHelper.taxHelper.getmTaxListByProductId().get(prod.getProductID());
                        if (taxList != null) {
                            for (int index = 0; index < taxList.size(); index++) {
                                taxPercentage += taxList.get(index).getTaxRate();
                            }
                        }
                    }
                    mProductValue = SDUtil.format(taxPercentage, 1, 0);
                } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_TAX_VALUE)) {
                    double productTaxAmount = 0;
                    if (bmodel.productHelper.taxHelper.getmTaxListByProductId() != null) {
                        ArrayList<TaxBO> taxList = bmodel.productHelper.taxHelper.getmTaxListByProductId().get(prod.getProductID());
                        if (taxList != null) {
                            for (int index = 0; index < taxList.size(); index++) {
                                productTaxAmount += taxList.get(index).getTotalTaxAmount();
                            }
                        }
                    }
                    mProductValue = formatValueInPrint(productTaxAmount, attr.getmAttributePrecision());
                } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_PROMO_TYPE)) {
                    mProductValue = getPromoType(context, prod);
                } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_FOC)) {
                    mProductValue = String.valueOf(prod.getFoc());
                    mProductQtyInPieceTotal = mProductQtyInPieceTotal + SDUtil.convertToInt(mProductValue);
                } else if (attr.getAttributeName().equalsIgnoreCase(TAG_DISCOUNTED_PRICE)) {
                    mProductValue = formatValueInPrint(prod.getLineValueAfterSchemeApplied() / prod.getTotalOrderedQtyInPieces(), attr.getmAttributePrecision());
                } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_SCHEME_DISCOUNT)) {
                    mProductValue = formatValueInPrint(prod.getSchemeDiscAmount(), attr.getmAttributePrecision());
                }


                if (!attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_NAME) || product_name_single_line.equalsIgnoreCase("NO")) {
                    if (mProductValue.length() > attr.getAttributeLength()) {
                        mProductValue = mProductValue.substring(0, attr.getAttributeLength() - attr.getAttributeSpecialChar().length()) + attr.getAttributeSpecialChar();
                    } else if (mProductValue.length() < attr.getAttributeLength()) {
                        int diff = attr.getAttributeLength() - mProductValue.length();

                        if (attr.getAttributePadding().equalsIgnoreCase(ALIGNMENT_RIGHT)) {
                            mProductValue = doAlign(mProductValue, ALIGNMENT_RIGHT, diff);
                        } else {
                            mProductValue = doAlign(mProductValue, ALIGNMENT_LEFT, diff);
                        }
                    }
                }
                mProductValue = doAlign(mProductValue, ALIGNMENT_RIGHT, attr.getAttributeSpace());

                sb.append(mProductValue);

                //print the text which is next to the product name into next line
                if (product_name_single_line.equalsIgnoreCase("YES")) {
                    // If product name is single line, then second line should be printed after the first column of first line
                    // So that bottom common labels will be aligned in straight to the first column(Ex:TAG_PRODUCT_LINE_TOTAL_WITH_QTY)..
                    mLengthUptoPName = mLengthUptoPName + attr.getAttributeLength() + attr.getAttributeSpace();
                    if (firstColumnWidth == 0) {
                        firstColumnWidth = attr.getAttributeLength() + attr.getAttributeSpace();
                    }
                    if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_NAME)) {
                        sb.append("\n");
                        char emptySpace = ' ';
                        for (int sp = 0; sp < firstColumnWidth; sp++) {
                            sb.append(emptySpace);
                        }
                    }
                }
            }

            sb.append("\n");

            //load the ordered product line item - end

            total_line_value_incl_tax = total_line_value_incl_tax + prod.getNetValue();

            totalPriceOffValue = totalPriceOffValue + prod.getTotalOrderedQtyInPieces() * prod.getPriceoffvalue();

            //load scheme free product
            if (isLoadSchemeFreeProduct.equalsIgnoreCase("YES")) {
                loadSchemeFreeProduct(prod, mAttrList, sb, product_name_single_line);
            }
        }

        sb.deleteCharAt(sb.length() - 1);
        total_net_payable = total_line_value_incl_tax;
    }

    /**
     * load the ordered products value from product object batch wise
     *
     * @param mAttrList
     * @param sb
     * @param isLoadSchemeFreeProduct - indicate to load scheme product
     */
    private void loadProductsWithBatch(Vector<AttributeListBO> mAttrList, StringBuilder sb, String isLoadSchemeFreeProduct, Vector<ProductMasterBO> productList, String product_name_single_line) {

        //Get the ordered product from product list
        //Vector<ProductMasterBO> productList = bmodel.productHelper.getProductMaster();
        int productsCount = productList.size();
        mOrderedProductList = new ArrayList<ProductMasterBO>();

        ProductMasterBO productBO;
        for (int i = 0; i < productsCount; i++) {
            productBO = productList.elementAt(i);
            if (productBO.getTotalOrderedQtyInPieces() > 0
                    || (bmodel.configurationMasterHelper.SHOW_SALES_RETURN_IN_ORDER && isReturnDoneForProduct(productBO))) {

                mOrderedProductList.add(productBO);
            }
        }


        sb.append("\n");
        String mProductValue;
        ArrayList<ProductMasterBO> batchList;
        int mLengthUptoPName;


        mProductCaseQtyTotal = 0;
        mProductPieceQtyTotal = 0;
        mProductOuterQtyTotal = 0;
        mProductQtyInPieceTotal = 0;

        mProductLineValueTotal = 0;
        mProductLineValueExcludingTaxTotal = 0;
        mProductLineValueIncludingTaxTotal = 0;

        mProductRetQtyTotal = 0;
        mProductRepQtyTotal = 0;
        mProductRepOrdInPieceTotal = 0;


        for (ProductMasterBO prod : mOrderedProductList) {

            if (prod.getBatchwiseProductCount() > 0
                    && (batchList = bmodel.batchAllocationHelper.getBatchlistByProductID().get(prod.getProductID())) != null) {

                for (ProductMasterBO batchProductBO : batchList) {
                    mLengthUptoPName = 0;
                    if (batchProductBO.getTotalOrderedQtyInPieces() > 0) {
                        for (AttributeListBO attr : mAttrList) {
                            mProductValue = "";
                            if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_CODE)) {
                                mProductValue = prod.getProductCode();
                            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_NAME)) {
                                mProductValue = (prod.getProductShortName() != null ? prod.getProductShortName() : prod.getProductName());
                            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_PRICE_CASE)) {
                                mProductValue = formatValueInPrint(batchProductBO.getCsrp(), attr.getmAttributePrecision());
                            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_PRICE_PIECE)) {
                                mProductValue = formatValueInPrint(batchProductBO.getSrp(), attr.getmAttributePrecision());
                            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_MRP)) {
                                mProductValue = formatValueInPrint(batchProductBO.getMRP(), attr.getmAttributePrecision());
                            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_PRICE_OUTER)) {
                                mProductValue = formatValueInPrint(batchProductBO.getOsrp(), attr.getmAttributePrecision());
                            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_QTY_CASE)) {
                                mProductValue = batchProductBO.getOrderedCaseQty() + "";
                                mProductCaseQtyTotal = mProductCaseQtyTotal + batchProductBO.getOrderedCaseQty();
                            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_QTY_PIECE)) {
                                mProductValue = batchProductBO.getOrderedPcsQty() + "";
                                mProductPieceQtyTotal = mProductPieceQtyTotal + batchProductBO.getOrderedPcsQty();
                            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_QTY_OUTER)) {
                                mProductValue = batchProductBO.getOrderedOuterQty() + "";
                                mProductOuterQtyTotal = mProductOuterQtyTotal + batchProductBO.getOrderedOuterQty();
                            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_UOM_WISE_QTY)) {
                                mProductValue = getUomWiseQty(batchProductBO);
                            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_QTY_TOTAL_IN_PIECE)) {
                                mProductValue = batchProductBO.getTotalOrderedQtyInPieces() + "";
                                mProductQtyInPieceTotal = mProductQtyInPieceTotal + (batchProductBO.getTotalOrderedQtyInPieces());
                            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_LINE_VALUE)) {
                                mProductValue = formatValueInPrint(batchProductBO.getLineValue(), attr.getmAttributePrecision());
                                mProductLineValueTotal = mProductLineValueTotal + SDUtil.convertToDouble(mProductValue);
                            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_LINE_VALUE_EXCLUDING_TAX)) {
                                mProductValue = formatValueInPrint(batchProductBO.getTaxableAmount() > 0 ? batchProductBO.getTaxableAmount() : batchProductBO.getNetValue(), attr.getmAttributePrecision());
                                mProductLineValueExcludingTaxTotal = mProductLineValueExcludingTaxTotal + SDUtil.convertToDouble(mProductValue);
                            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_lINE_VALUE_INCLUDING_TAX)) {
                                mProductValue = formatValueInPrint(batchProductBO.getNetValue(), attr.getmAttributePrecision());
                                mProductLineValueIncludingTaxTotal = mProductLineValueIncludingTaxTotal + SDUtil.convertToDouble(mProductValue);
                            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_REPLACE_QTY_PIECE)) {
                                mProductValue = batchProductBO.getRepPieceQty() + (batchProductBO.getRepCaseQty() * batchProductBO.getCaseSize()) + (batchProductBO.getRepOuterQty() * batchProductBO.getOutersize()) + "";
                                mProductRepQtyTotal = mProductRepQtyTotal + Integer.parseInt(mProductValue.replace(",", ""));
                            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_RETURN_QTY_PIECE)) {
                                int lineValue = 0;
                                if (batchProductBO.getSalesReturnReasonList().size() > 0) {
                                    for (SalesReturnReasonBO obj : batchProductBO.getSalesReturnReasonList())
                                        lineValue = lineValue + obj.getPieceQty() + (obj.getCaseQty() * obj.getCaseSize()) + (obj.getOuterQty() * obj.getOuterSize());
                                }
                                mProductValue = lineValue + "";
                                mProductRetQtyTotal = mProductRetQtyTotal + Integer.parseInt(mProductValue.replace(",", ""));
                            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_SUM_QTY_PIECE_WITH_REP)) {
                                mProductValue = batchProductBO.getRepPieceQty() + (batchProductBO.getRepCaseQty() * batchProductBO.getCaseSize()) + (batchProductBO.getRepOuterQty() * batchProductBO.getOutersize()) + (batchProductBO.getOrderedPcsQty() + (batchProductBO.getOrderedCaseQty() * batchProductBO.getCaseSize()) + (batchProductBO.getOrderedOuterQty() * batchProductBO.getOutersize())) + "";
                                mProductRepOrdInPieceTotal = mProductRepOrdInPieceTotal + Integer.parseInt(mProductValue.replace(",", ""));
                            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_TAG_DESC)) {
                                mProductValue = prod.getDescription() + "";
                            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_HSN_CODE)) {
                                mProductValue = prod.getProductCode();
                            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_PROMO_TYPE)) {
                                mProductValue = getPromoType(context, prod);
                            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_FOC)) {
                                mProductValue = String.valueOf(prod.getFoc());
                                mProductQtyInPieceTotal = mProductQtyInPieceTotal + SDUtil.convertToInt(mProductValue);
                            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_DISCOUNTED_PRICE)) {
                                mProductValue = formatValueInPrint(prod.getLineValueAfterSchemeApplied() / prod.getTotalOrderedQtyInPieces(), attr.getmAttributePrecision());
                            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_SCHEME_DISCOUNT)) {
                                mProductValue = formatValueInPrint(prod.getSchemeDiscAmount(), attr.getmAttributePrecision());
                            }

                            if (!attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_NAME) || product_name_single_line.equalsIgnoreCase("NO")) {
                                if (mProductValue.length() > attr.getAttributeLength()) {
                                    mProductValue = mProductValue.substring(0, attr.getAttributeLength() - attr.getAttributeSpecialChar().length()) + attr.getAttributeSpecialChar();
                                } else if (mProductValue.length() < attr.getAttributeLength()) {
                                    int diff = attr.getAttributeLength() - mProductValue.length();

                                    if (attr.getAttributePadding().equalsIgnoreCase(ALIGNMENT_RIGHT)) {
                                        mProductValue = doAlign(mProductValue, ALIGNMENT_RIGHT, diff);
                                    } else {
                                        mProductValue = doAlign(mProductValue, ALIGNMENT_LEFT, diff);
                                    }

                                }
                            }
                            mProductValue = doAlign(mProductValue, ALIGNMENT_RIGHT, attr.getAttributeSpace());

                            sb.append(mProductValue);

                            //print the text which is next to the product name into next line
                            if (product_name_single_line.equalsIgnoreCase("YES")) {
                                mLengthUptoPName = mLengthUptoPName + attr.getAttributeLength() + attr.getAttributeSpace();

                                if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_NAME)) {
                                    sb.append("\n");
                                    if (batchProductBO.getBatchNo().equalsIgnoreCase("NA") || batchProductBO.getBatchNo().equalsIgnoreCase("none"))
                                        sb.append(" - - -");
                                    else {
                                        sb.append(batchProductBO.getBatchNo());
                                    }
                                    sb.append("\n");
                                    char emptySpace = ' ';
                                    for (int sp = 0; sp < mLengthUptoPName; sp++) {
                                        sb.append(emptySpace);
                                    }
                                }
                            }
                        }
                        if (product_name_single_line.equalsIgnoreCase("NO")) {
                            sb.append("\n");

                            if (batchProductBO.getBatchNo().equalsIgnoreCase("NA") || batchProductBO.getBatchNo().equalsIgnoreCase("none"))
                                sb.append(" - - -");
                            else {
                                sb.append(batchProductBO.getBatchNo());
                            }
                            sb.append("\n");
                        }

                        total_line_value_incl_tax = total_line_value_incl_tax +
                                SDUtil.convertToDouble(SDUtil.format(batchProductBO.getNetValue(), 2, 0));

                        totalPriceOffValue = totalPriceOffValue + (batchProductBO.getTotalOrderedQtyInPieces() * batchProductBO.getPriceoffvalue());
                    }
                }//

                //load scheme free product
                if (isLoadSchemeFreeProduct.equalsIgnoreCase("YES")) {
                    loadSchemeFreeProduct(prod, mAttrList, sb, product_name_single_line);
                }

            }
        }

        sb.deleteCharAt(sb.length() - 1);
        total_net_payable = total_line_value_incl_tax;
    }


    /**
     * @param prod      - ordered product
     * @param mAttrList - value to be display for free products
     * @param sb        - output
     */
    private void loadSchemeFreeProduct(ProductMasterBO prod, Vector<AttributeListBO> mAttrList, StringBuilder sb, String product_name_single_line) {
        int mLengthUptoPName;
        if (prod.getSchemeProducts() != null) {
            List<SchemeProductBO> freeProductList = prod.getSchemeProducts();
            if (freeProductList != null) {
                for (SchemeProductBO schemeProductBO : freeProductList) {
                    ProductMasterBO freeProduct = bmodel.productHelper
                            .getProductMasterBOById(schemeProductBO
                                    .getProductId());

                    if (freeProduct != null) {
                        String mProductValue;
                        mLengthUptoPName = 0;
                        for (AttributeListBO attr : mAttrList) {
                            mProductValue = "";
                            if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_CODE)) {
                                mProductValue = freeProduct.getProductCode();
                            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_NAME)) {
                                mProductValue = (freeProduct.getProductShortName() != null ? freeProduct.getProductShortName() : freeProduct.getProductName());
                            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_QTY_CASE)) {
                                if (freeProduct.getCaseUomId() == schemeProductBO.getUomID()
                                        && freeProduct.getCaseUomId() != 0) {
                                    mProductValue = schemeProductBO.getQuantitySelected() + "";
                                    mProductCaseQtyTotal += mProductCaseQtyTotal + schemeProductBO.getQuantitySelected();
                                }
                            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_QTY_PIECE)) {
                                if (freeProduct.getPcUomid() == schemeProductBO.getUomID()
                                        && freeProduct.getPcUomid() != 0) {
                                    mProductValue = schemeProductBO.getQuantitySelected() + "";
                                    mProductPieceQtyTotal = mProductPieceQtyTotal + schemeProductBO.getQuantitySelected();
                                }
                            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_QTY_OUTER)) {
                                if (freeProduct.getOuUomid() == schemeProductBO.getUomID()
                                        && freeProduct.getOuUomid() != 0) {
                                    mProductValue = schemeProductBO.getQuantitySelected() + "";
                                    mProductOuterQtyTotal += mProductOuterQtyTotal + schemeProductBO.getQuantitySelected();
                                }
                            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_UOM_WISE_QTY)) {

                                if ((freeProduct.getPcUomid() == schemeProductBO.getUomID() && freeProduct.getPcUomid() != 0)
                                        || (freeProduct.getCaseUomId() == schemeProductBO.getUomID() && freeProduct.getCaseUomId() != 0)
                                        || (freeProduct.getOuUomid() == schemeProductBO.getUomID() && freeProduct.getOuUomid() != 0))
                                    mProductValue = schemeProductBO.getQuantitySelected() + " " + schemeProductBO.getUomDescription();
                            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_QTY_TOTAL_IN_PIECE)) {
                                if (freeProduct.getCaseUomId() == schemeProductBO.getUomID()
                                        && freeProduct.getCaseUomId() != 0) {
                                    mProductValue = (freeProduct.getCaseSize() * schemeProductBO.getQuantitySelected()) + "";
                                }
                                if (freeProduct.getOuUomid() == schemeProductBO.getUomID()
                                        && freeProduct.getOuUomid() != 0) {
                                    mProductValue = (freeProduct.getOutersize() * schemeProductBO.getQuantitySelected()) + "";
                                }
                                if (freeProduct.getPcUomid() == schemeProductBO.getUomID()
                                        && freeProduct.getPcUomid() != 0) {
                                    mProductValue = schemeProductBO.getQuantitySelected() + "";
                                }
                                mProductQtyInPieceTotal = mProductQtyInPieceTotal + SDUtil.convertToInt(mProductValue);
                            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_LINE_VALUE)) {
                                mProductValue = formatValueInPrint(schemeProductBO.getLineValue(), attr.getmAttributePrecision());
                            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_LINE_VALUE_EXCLUDING_TAX)) {
                                mProductValue = formatValueInPrint((schemeProductBO.getLineValue() - schemeProductBO.getTaxAmount()), attr.getmAttributePrecision());
                            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_lINE_VALUE_INCLUDING_TAX)) {
                                mProductValue = formatValueInPrint(schemeProductBO.getLineValue(), attr.getmAttributePrecision());
                            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_HSN_CODE)) {
                                mProductValue = prod.getProductCode();
                            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_PROMO_TYPE)) {
                                mProductValue = context.getResources().getString(R.string.free);
                            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_PRICE_PIECE)) {
                                mProductValue = formatValueInPrint(0, attr.getmAttributePrecision());
                            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_SCHEME_DISCOUNT)) {
                                mProductValue = formatValueInPrint(0, attr.getmAttributePrecision());
                            }

                            if (!attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_NAME) || product_name_single_line.equalsIgnoreCase("NO")) {
                                if (mProductValue.length() > attr.getAttributeLength()) {
                                    mProductValue = mProductValue.substring(0, attr.getAttributeLength() - attr.getAttributeSpecialChar().length()) + attr.getAttributeSpecialChar();
                                } else if (mProductValue.length() < attr.getAttributeLength()) {
                                    int diff = attr.getAttributeLength() - mProductValue.length();

                                    if (attr.getAttributePadding().equalsIgnoreCase(ALIGNMENT_RIGHT)) {
                                        mProductValue = doAlign(mProductValue, ALIGNMENT_RIGHT, diff);
                                    } else {
                                        mProductValue = doAlign(mProductValue, ALIGNMENT_LEFT, diff);
                                    }
                                }
                            }
                            mProductValue = doAlign(mProductValue, ALIGNMENT_RIGHT, attr.getAttributeSpace());

                            sb.append(mProductValue);
                            //print the text which is next to the product name into next line
                            if (product_name_single_line.equalsIgnoreCase("YES")) {
                                mLengthUptoPName = mLengthUptoPName + attr.getAttributeLength() + attr.getAttributeSpace();
                                if (firstColumnWidth == 0)
                                    firstColumnWidth = attr.getAttributeLength() + attr.getAttributeSpace();

                                if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_NAME)) {
                                    sb.append("\n");
                                    char emptySpace = ' ';
                                    for (int sp = 0; sp < firstColumnWidth; sp++) {
                                        sb.append(emptySpace);
                                    }
                                }
                            }
                        }
                        sb.append("\n");
                    }
                }
            }
        }

    }

    private void calculateSchemeAmountDiscountValue() {

        SchemeDetailsMasterHelper schemeHelper = SchemeDetailsMasterHelper.getInstance(context);
        ArrayList<SchemeBO> appliedSchemeList = schemeHelper.getAppliedSchemeList();
        double mBuyProdDiscountedValue = 0;
        if (appliedSchemeList != null) {
            for (SchemeBO schemeBO : appliedSchemeList) {
                if (schemeBO != null) {

                    if (schemeBO.isAmountTypeSelected()) {
                        netSchemeAmount += schemeBO.getSelectedAmount();
                    } else if (schemeBO.isDiscountPrecentSelected() || schemeBO.isPriceTypeSeleted()) {
                        for (SchemeProductBO buyProd : schemeBO.getBuyingProducts()) {
                            ProductMasterBO productBO = bmodel.productHelper.getProductMasterBOById(buyProd.getProductId());
                            if (productBO != null) {
                                if (productBO.getTotalOrderedQtyInPieces() > 0) {
                                    mBuyProdDiscountedValue = mBuyProdDiscountedValue + buyProd.getDiscountValue();
                                }
                            }
                        }

                        netSchemeAmount += mBuyProdDiscountedValue;
                    }


                }
            }

        }
    }

    private void loadSchemeDiscount(Vector<AttributeListBO> mAttrList, String product_name_single_line, StringBuilder sb) {
        int mLengthUptoPName;

        SchemeDetailsMasterHelper schemeHelper = SchemeDetailsMasterHelper.getInstance(context);
        ArrayList<SchemeBO> appliedSchemeList = schemeHelper.getAppliedSchemeList();
        if (appliedSchemeList != null) {
            for (SchemeBO schemeBO : appliedSchemeList) {
                if (schemeBO != null) {

                    String mProductValue = "";
                    String schemeName = "";
                    double mBuyProdDiscountedValue = 0, schemeValue = 0;
                    mLengthUptoPName = 0;

                    if (schemeBO.isAmountTypeSelected()) {

                        schemeName = schemeBO.getProductName();
                        schemeValue = schemeBO.getSelectedAmount();
                        netSchemeAmount += schemeValue;

                    } else if (schemeBO.isDiscountPrecentSelected() || schemeBO.isPriceTypeSeleted()) {

                        schemeName = schemeBO.getProductName();
                        for (SchemeProductBO buyProd : schemeBO.getBuyingProducts()) {
                            ProductMasterBO productBO = bmodel.productHelper.getProductMasterBOById(buyProd.getProductId());
                            if (productBO != null) {
                                if (productBO.getTotalOrderedQtyInPieces() > 0) {
                                    mBuyProdDiscountedValue = mBuyProdDiscountedValue + buyProd.getDiscountValue();
                                }
                            }
                        }

                        schemeValue = mBuyProdDiscountedValue;
                        netSchemeAmount += schemeValue;
                    }


                    if (schemeBO.isAmountTypeSelected() || schemeBO.isDiscountPrecentSelected() || schemeBO.isPriceTypeSeleted()) {

                        for (AttributeListBO attr : mAttrList) {
                            mProductValue = "";

                            if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_NAME)) {
                                mProductValue = schemeName;
                            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_SCHEME_DISCOUNT)) {
                                mProductValue = formatValueInPrint(schemeValue, attr.getmAttributePrecision());
                            }

                            if (!attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_NAME) || product_name_single_line.equalsIgnoreCase("NO")) {
                                if (mProductValue.length() > attr.getAttributeLength()) {
                                    mProductValue = mProductValue.substring(0, attr.getAttributeLength() - attr.getAttributeSpecialChar().length()) + attr.getAttributeSpecialChar();
                                } else if (mProductValue.length() < attr.getAttributeLength()) {
                                    int diff = attr.getAttributeLength() - mProductValue.length();

                                    if (attr.getAttributePadding().equalsIgnoreCase(ALIGNMENT_RIGHT)) {
                                        mProductValue = doAlign(mProductValue, ALIGNMENT_RIGHT, diff);
                                    } else {
                                        mProductValue = doAlign(mProductValue, ALIGNMENT_LEFT, diff);
                                    }
                                }
                            }
                            mProductValue = doAlign(mProductValue, ALIGNMENT_RIGHT, attr.getAttributeSpace());

                            sb.append(mProductValue);

                            //print the text which is next to the product name into next line
                            if (product_name_single_line.equalsIgnoreCase("YES")) {
                                mLengthUptoPName = mLengthUptoPName + attr.getAttributeLength() + attr.getAttributeSpace();

                                if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_NAME)) {
                                    sb.append("\n");
                                    char emptySpace = ' ';
                                    for (int sp = 0; sp < mLengthUptoPName; sp++) {
                                        sb.append(emptySpace);
                                    }
                                }
                            }
                        }
                        sb.append("\n");
                    }


                }
            }

        }
    }

    /**
     * Load product level discount applied from master
     *
     * @return
     */
    private String getProductLevelApplyDiscount(String label, int precision) {
        StringBuffer sb = new StringBuffer();

        DiscountHelper discountHelper = DiscountHelper.getInstance(context);

        HashMap<String, Double> discountList = discountHelper.prepareProductDiscountForPrint(context, orderHelper.getOrderId().replaceAll("\'", ""));

        if (discountList.size() > 0) {
            for (String discountName : discountList.keySet()) {
                sb.append(alignWithLabelForSingleLine(discountName, formatValueInPrint(discountList.get(discountName), precision)));
                sb.append("\n");

            }
        } else {
            sb.append(alignWithLabelForSingleLine(label, formatValueInPrint(0, precision)));
            sb.append("\n");
        }

        return sb.toString();
    }


    /**
     * load product level tax which is applied
     *
     * @return
     */
    private String getProductLevelTax(String label, int precision) {

        StringBuilder sb = new StringBuilder();

        HashMap<String, Double> taxList = bmodel.productHelper.taxHelper.prepareProductTaxForPrint(context, orderHelper.getOrderId().replaceAll("\'", ""), isFromInvoiceTransaction);
        if (taxList.size() > 0) {
            for (String taxName : taxList.keySet()) {
                sb.append(alignWithLabelForSingleLine(taxName, formatValueInPrint(taxList.get(taxName), precision)));
                sb.append("\n");
            }
        } else {
            // Printing tax value as 0.
            sb.append(alignWithLabelForSingleLine(label, formatValueInPrint(0, precision)));
            sb.append("\n");
        }

        return sb.toString();

    }


    /**
     * get bill level tax
     *
     * @return
     */
    private String printBillLevelTax(int precision, String attr_space) {
        StringBuffer sb = new StringBuffer();
        int extraSpace = 0;
        try {
            extraSpace = SDUtil.convertToInt(attr_space);
            final ArrayList<TaxBO> taxList = bmodel.productHelper.taxHelper.getBillTaxList();
            if (taxList != null && taxList.size() > 0) {
                if (bmodel.configurationMasterHelper.SHOW_INCLUDE_BILL_TAX) {

                    for (int index = 0; index < taxList.size(); index++) {
                        TaxBO taxBO = taxList.get(index);

                        //Displaying list of taxes applied.
                        sb.append(alignWithLabelForSingleLine(taxBO.getTaxDesc() + "(" + taxBO.getTaxRate() + "%) :", formatValueInPrint(taxBO.getTotalTaxAmount(), precision) + "", extraSpace));
                        if (index < taxList.size() - 1)
                            sb.append("\n");
                    }

                } else {

                    // Here bill amount excluding tax value calculated because this is only needed in print.
                    double totalTaxRate = 0;
                    for (TaxBO taxBO : taxList) {
                        totalTaxRate = totalTaxRate + taxBO.getTaxRate();
                    }
                    double mTotalIncludeTax = total_line_value_incl_tax - mBillLevelDiscountValue;
                    double mTotalExcludeTaxAmount = mTotalIncludeTax / (1 + totalTaxRate / 100);

                    sb.append(alignWithLabelForSingleLine("Excl Tax:", "" + formatValueInPrint(mTotalExcludeTaxAmount, precision)));
                    sb.append("\n");

                    //Displaying list of taxes applied.
                    for (TaxBO taxBO : taxList) {
                        sb.append(alignWithLabelForSingleLine(taxBO.getTaxDesc() + "(" + taxBO.getTaxRate() + ")", taxBO.getTotalTaxAmount() + ""));
                        sb.append("\n");
                    }
                }
            }

        } catch (Exception e) {
            Commons.printException(e);
        }
        return sb.toString();
    }

    private void getEmptyReturnValue() {

        ArrayList<BomReturnBO> mEmptyProducts;
        double totalEmp = 0;

        if (bmodel.configurationMasterHelper.SHOW_GROUPPRODUCTRETURN)
            mEmptyProducts = bmodel.productHelper
                    .getBomReturnTypeProducts();
        else
            mEmptyProducts = bmodel.productHelper.getBomReturnProducts();

        if (mEmptyProducts != null && mEmptyProducts.size() > 0) {
            Collections.sort(mEmptyProducts, BomReturnBO.SKUWiseAscending);
            for (BomReturnBO productBO : mEmptyProducts) {
                totalEmp = (productBO.getLiableQty() * productBO.getpSrp()) - (productBO.getReturnQty() * productBO.getpSrp());
                mEmptyTotalValue = mEmptyTotalValue + totalEmp;
            }
        }
    }


    private void getCollectionBeforeInvValue() {
        List<PaymentBO> payment = CollectionHelper.getInstance(context).getPaymentList();
        if (payment != null && payment.size() > 0) {

            switch (payment.get(0).getCashMode()) {
                case StandardListMasterConstants.CHEQUE:
                    mCheque = payment.get(0).getAmount();
                    break;
                case StandardListMasterConstants.CASH:
                    mCash = payment.get(0).getAmount();
                    break;
                case StandardListMasterConstants.CREDIT_NOTE:
                    mCreditNoteValue = payment.get(0).getAmount();
                    break;
            }


        }
    }


    private String getCollectionAmount(String label, int precisionCount) {
        StringBuffer sb = new StringBuffer();
        String mode = "";
        String s = ""; // load collection paid amount's
        if (mCash > 0) {
            mode = " (" + context.getString(R.string.cash) + ")";
            s = alignWithLabelForSingleLine((label + mode), formatValueInPrint(mCash, precisionCount));
            if (!s.isEmpty())
                sb.append(s);

        }
        if (mCheque > 0) {
            if (mCash > 0)
                sb.append("\n");
            mode = " (" + context.getString(R.string.cheque) + ")";
            s = "";
            s = alignWithLabelForSingleLine((label + mode), formatValueInPrint(mCheque, precisionCount));
            if (!s.isEmpty())
                sb.append(s);

        }
        if (mCreditNoteValue > 0) {
            if (mCash > 0 || mCheque > 0)
                sb.append("\n");
            mode = " (" + context.getString(R.string.credit_note) + ")";
            s = "";
            s = alignWithLabelForSingleLine((label + mode), formatValueInPrint(mCreditNoteValue, precisionCount));
            if (!s.isEmpty())
                sb.append(s);

        }
        return sb.toString();
    }


    /**
     * load empty return details
     *
     * @param mAttrList - define value to be displayed
     * @param sb        - source and output
     */
    private void printEmptyReturn(Vector<AttributeListBO> mAttrList, String empty_product_name_single_line, StringBuilder sb) {

        sb.append("\n");

        ArrayList<BomReturnBO> mEmptyProducts;
        int mLengthUptoPName;
        //double totalEmp = 0 , mLiableTot = 0, mReturnTot = 0;

        if (bmodel.configurationMasterHelper.SHOW_GROUPPRODUCTRETURN)
            mEmptyProducts = bmodel.productHelper
                    .getBomReturnTypeProducts();
        else
            mEmptyProducts = bmodel.productHelper.getBomReturnProducts();


        if (mEmptyProducts != null && mEmptyProducts.size() > 0) {

            //sb.append("\n");

            Collections.sort(mEmptyProducts, BomReturnBO.SKUWiseAscending);
            String mProductValue = "";
            emptyFirstColumnWidth = 0;
            //Liable
            for (BomReturnBO prod : mEmptyProducts) {
                mLengthUptoPName = 0;
                if ((prod.getLiableQty() > 0)) {
                    for (AttributeListBO attr : mAttrList) {
                        mProductValue = "";
                        if (attr.getAttributeName().equalsIgnoreCase(EMPTY_PRODUCT_NAME)) {
                            mProductValue = "ED - " + (prod.getProductShortName() != null ? prod.getProductShortName() : prod.getProductName());
                        } else if (attr.getAttributeName().equalsIgnoreCase(EMPTY_PRODUCT_QTY)) {
                            mProductValue = prod.getLiableQty() + "";
                        } else if (attr.getAttributeName().equalsIgnoreCase(EMPTY_PRODUCT_PRICE)) {
                            mProductValue = formatValueInPrint(prod.getpSrp(), attr.getmAttributePrecision());
                        } else if (attr.getAttributeName().equalsIgnoreCase(EMPTY_PRODUCT_LINE_VALUE)) {
                            mProductValue = formatValueInPrint(prod.getLiableQty() * prod.getpSrp(), attr.getmAttributePrecision());
                        }
                        if (!attr.getAttributeName().equalsIgnoreCase(EMPTY_PRODUCT_NAME) ||
                                empty_product_name_single_line.equalsIgnoreCase("NO")) {
                            if (mProductValue.length() > attr.getAttributeLength()) {
                                mProductValue = mProductValue.substring(0, attr.getAttributeLength() - attr.getAttributeSpecialChar().length()) + attr.getAttributeSpecialChar();
                            } else if (mProductValue.length() < attr.getAttributeLength()) {
                                int diff = attr.getAttributeLength() - mProductValue.length();

                                if (attr.getAttributePadding().equalsIgnoreCase(ALIGNMENT_RIGHT)) {
                                    mProductValue = doAlign(mProductValue, ALIGNMENT_RIGHT, diff);
                                } else {
                                    mProductValue = doAlign(mProductValue, ALIGNMENT_LEFT, diff);
                                }
                            }
                        }
                        mProductValue = doAlign(mProductValue, ALIGNMENT_RIGHT, attr.getAttributeSpace());

                        sb.append(mProductValue);

                        //print the text which is next to the product name into next line
                        if (empty_product_name_single_line.equalsIgnoreCase("YES")) {
                            mLengthUptoPName = mLengthUptoPName + attr.getAttributeLength() + attr.getAttributeSpace();
                            if (emptyFirstColumnWidth == 0) {
                                emptyFirstColumnWidth = attr.getAttributeLength() + attr.getAttributeSpace();
                            }
                            if (attr.getAttributeName().equalsIgnoreCase(EMPTY_PRODUCT_NAME)) {
                                sb.append("\n");
                                char emptySpace = ' ';
                                for (int sp = 0; sp < emptyFirstColumnWidth; sp++) {
                                    sb.append(emptySpace);
                                }
                            }
                        }

                    }
                    sb.append("\n");
                }
            }

            emptyFirstColumnWidth = 0;
            for (BomReturnBO prod : mEmptyProducts) {
                mLengthUptoPName = 0;
                if ((prod.getReturnQty() > 0)) {
                    for (AttributeListBO attr : mAttrList) {
                        mProductValue = "";
                        if (attr.getAttributeName().equalsIgnoreCase(EMPTY_PRODUCT_NAME)) {
                            mProductValue = "ER - " + (prod.getProductShortName() != null ? prod.getProductShortName() : prod.getProductName());
                        } else if (attr.getAttributeName().equalsIgnoreCase(EMPTY_PRODUCT_QTY)) {
                            mProductValue = formatValueInPrint(prod.getReturnQty(), attr.getmAttributePrecision());
                        } else if (attr.getAttributeName().equalsIgnoreCase(EMPTY_PRODUCT_PRICE)) {
                            mProductValue = formatValueInPrint(prod.getpSrp(), attr.getmAttributePrecision());
                        } else if (attr.getAttributeName().equalsIgnoreCase(EMPTY_PRODUCT_LINE_VALUE)) {
                            mProductValue = "-" + formatValueInPrint(prod.getReturnQty() * prod.getpSrp(), attr.getmAttributePrecision());
                        }

                        if (!attr.getAttributeName().equalsIgnoreCase(EMPTY_PRODUCT_NAME) ||
                                empty_product_name_single_line.equalsIgnoreCase("NO")) {
                            if (mProductValue.length() > attr.getAttributeLength()) {
                                mProductValue = mProductValue.substring(0, attr.getAttributeLength() - attr.getAttributeSpecialChar().length()) + attr.getAttributeSpecialChar();
                            } else if (mProductValue.length() < attr.getAttributeLength()) {
                                int diff = attr.getAttributeLength() - mProductValue.length();

                                if (attr.getAttributePadding().equalsIgnoreCase(ALIGNMENT_RIGHT)) {
                                    mProductValue = doAlign(mProductValue, ALIGNMENT_RIGHT, diff);
                                } else {
                                    mProductValue = doAlign(mProductValue, ALIGNMENT_LEFT, diff);
                                }
                            }
                        }
                        mProductValue = doAlign(mProductValue, ALIGNMENT_RIGHT, attr.getAttributeSpace());

                        sb.append(mProductValue);

                        //print the text which is next to the product name into next line
                        if (empty_product_name_single_line.equalsIgnoreCase("YES")) {
                            mLengthUptoPName = mLengthUptoPName + attr.getAttributeLength() + attr.getAttributeSpace();
                            if (emptyFirstColumnWidth == 0) {
                                emptyFirstColumnWidth = attr.getAttributeLength() + attr.getAttributeSpace();
                            }
                            if (attr.getAttributeName().equalsIgnoreCase(EMPTY_PRODUCT_NAME)) {
                                sb.append("\n");
                                char emptySpace = ' ';
                                for (int sp = 0; sp < emptyFirstColumnWidth; sp++) {
                                    sb.append(emptySpace);
                                }
                            }
                        }

                    }
                    sb.append("\n");
                }
            }
        }
    }

    /**
     * convert the value into words
     *
     * @param formatTotal - value
     * @return - string value for teh input
     */
    private String getAmountInWords(String formatTotal) {
        StringBuffer convertBuffer = new StringBuffer();

        if (formatTotal.length() <= 12) {
            String[] splits = formatTotal.split(Pattern.quote("."));
            NumberToWord numberToWord = new NumberToWord();
            for (int i = 0; i < splits.length; i++) {
                long splitvalue = SDUtil.convertToLong(splits[i]);
                if (i == 1 && splitvalue > 0) {
                    convertBuffer.append(" and ");
                }
                convertBuffer.append(numberToWord.convertNumberToWords(SDUtil.convertToLong(splits[i].toString())));
                if (i == 0) {
                    convertBuffer.append(" Rupees ");
                } else if (i == 1) {
                    if (!splits[i].toString().equals("00"))
                        convertBuffer.append(" Paise");
                }

            }
        }

        return convertBuffer.toString();
    }

    /**
     * align the value based on input
     * add the gap number of space in prefix if alignment is right, else add in suffix
     *
     * @param strValue  - value to be aligned
     * @param alignment - indicate left/right
     * @param gap       - (allocated space - actual space)
     * @return
     */
    private String doAlign(String strValue, String alignment, int gap) {
        String value = "";
        char emptySpace = ' ';

        if (alignment.equalsIgnoreCase(ALIGNMENT_RIGHT)) {
            for (int j = 0; j < gap; j++)
                value = value + emptySpace;

            value = value + strValue;
        } else {
            value = value + strValue;

            for (int j = 0; j < gap; j++)
                value = value + emptySpace;
        }
        return value;
    }

    /**
     * if given value is reach more than of attr length than wrap text into next line
     * call doAlign() for alignment
     *
     * @param strValue    - given string value
     * @param attr_length - String value length
     * @param alignment   - Text Alignment (indicate left/right)
     * @param gap         - starting  position of the text (allocated space - actual space)
     *                    Not : if it is negative value than will start in first position
     * @param strBold     - yes -> Bold apply  No -> Bold not apply
     * @return
     */
    private String doAlignWithNextLine(String strValue, int attr_length, String alignment, int gap, String strBold) {
        String strBoldTxt = "";
        String nexLine = "\n";
        int lastIndex = attr_length;
        if (strBold.equalsIgnoreCase("yes"))
            strBoldTxt = "#B#";
        StringBuilder mAttrValue = new StringBuilder();
        mAttrValue.append(strBoldTxt).append(doAlign(strValue.substring(0, attr_length), alignment, gap));
        int length = strValue.length() - attr_length;


        while (length > 0) {

            mAttrValue.append(nexLine);
            if (strValue.substring(lastIndex).length() <= attr_length) {
                mAttrValue.append(strBoldTxt).append(doAlign(strValue.substring(lastIndex), alignment, gap));
                length = 0;
            } else {
                mAttrValue.append(strBoldTxt).append(doAlign(strValue.substring(lastIndex, (lastIndex + attr_length)), alignment, gap));
                length -= lastIndex;
                lastIndex = lastIndex + attr_length;
            }
        }
        return mAttrValue.toString();
    }

    /**
     * align the entire line with label like total, tax, discount etc
     * it may come more than one line
     *
     * @param label
     * @param value
     * @return
     */
    private String alignWithLabel(String label, String value) {

        String s = label + value;

        int startPosition = mPaperLenghtInChar - s.length();
        value = doAlign(s, ALIGNMENT_RIGHT, startPosition);

        return value;
    }

    private String alignWithLabelForSingleLine(String label, String value) {

        String s = label + value;

        int startPosition = mPaperLenghtInChar - s.length();

        value = doAlign(value, ALIGNMENT_RIGHT, startPosition);

        return label + value;
    }


    private String alignWithLabelForSingleLine(String label, String value, int extraSpace) {

        String s = label + value;

        int startPosition = mPaperLenghtInChar - s.length() + extraSpace;

        value = doAlign(value, ALIGNMENT_RIGHT, startPosition);

        return label + value;
    }

    private class AttributeListBO {
        String mAttributeName;
        String mAttributeText;
        String mAttributeFontSize;
        int mAttributeLength;
        String mAttributePadding;
        int mAttributeSpace;
        String mAttributeSpecialChar;
        int mAttributePrecision;

        public String getAttributeName() {
            return mAttributeName;
        }

        public void setAttributeName(String mAttributeName) {
            this.mAttributeName = mAttributeName;
        }

        public String getAttributeText() {
            return mAttributeText;
        }

        public void setAttributeText(String mAttributeText) {
            this.mAttributeText = mAttributeText;
        }

        public int getAttributeLength() {
            return mAttributeLength;
        }

        public void setAttributeLength(int mAttributeLength) {
            this.mAttributeLength = mAttributeLength;
        }

        public String getAttributePadding() {
            return mAttributePadding;
        }

        public void setAttributePadding(String mAttributePadding) {
            this.mAttributePadding = mAttributePadding;
        }

        public int getAttributeSpace() {
            return mAttributeSpace;
        }

        public void setAttributeSpace(int mAttributeSpace) {
            this.mAttributeSpace = mAttributeSpace;
        }

        public String getAttributeSpecialChar() {
            return mAttributeSpecialChar;
        }

        public void setAttributeSpecialChar(String mAttributeSpecialChar) {
            this.mAttributeSpecialChar = mAttributeSpecialChar;
        }

        public int getmAttributePrecision() {
            return mAttributePrecision;
        }

        public void setmAttributePrecision(int mAttributePrecision) {
            this.mAttributePrecision = mAttributePrecision;
        }
    }

    private void resetValues() {
        mInvoiceData = new StringBuilder();
        total_line_value_incl_tax = 0;
        mBillLevelDiscountValue = 0;
        mEmptyTotalValue = 0;
        total_net_payable = 0;
        totalPriceOffValue = 0;
        isLogoEnabled = false;
        isSignatureEnabled = false;
        mSchemeValueByAmountType = 0;
        netSchemeAmount = 0;

        mLoadStockTotal = 0;
        mSoldStockTotal = 0;
        mFreeStockTotal = 0;
        mCurrentStockTotal = 0;
        mEmptyStockTotal = 0;
        mReturnStockTotal = 0;
        mRepStockTotal = 0;
        mNonSalableTotal = 0;
    }

    private String formatValueInPrint(double value, int precision) {
        int mPrecision;
        if (precision > -1) {
            mPrecision = precision;
        } else if (mGlobalPrecision > -1) {
            mPrecision = mGlobalPrecision;
        } else {
            mPrecision = bmodel.configurationMasterHelper.VALUE_PRECISION_COUNT;
        }

        return SDUtil.format(value,
                mPrecision,
                bmodel.configurationMasterHelper.VALUE_COMMA_COUNT, bmodel.configurationMasterHelper.IS_DOT_FOR_GROUP);
    }

    private String formatSalesValueInPrint(double value, int precision) {

        String formattedValue = "0";
        try {
            if (bmodel.configurationMasterHelper.IS_FORMAT_USING_CURRENCY_VALUE) {
                if (bmodel.configurationMasterHelper.IS_APPLY_CURRENCY_CONFIG) {
                    // getting currency config value for decimal value..

                    String tempVal;
                    String fractionalStr;

                    tempVal = value + "";
                    fractionalStr = tempVal.substring(tempVal.indexOf('.') + 1);
                    fractionalStr = (fractionalStr.length() > 2 ? fractionalStr.substring(0, 2) : fractionalStr);
                    if (fractionalStr.length() == 1)
                        fractionalStr = fractionalStr + "0";

                    int integerValue = (int) value;
                    int fractionValue = SDUtil.convertToInt(fractionalStr);

                    formattedValue = (integerValue + bmodel.getCurrencyActualValue(fractionValue) + "");


                } else {
                    formattedValue = SDUtil.format(value, 0, 0);

                }
            } else {
                // format normally
                int mPrecision;
                if (precision > -1) {
                    mPrecision = precision;
                } else if (mGlobalPrecision > -1) {
                    mPrecision = mGlobalPrecision;
                } else {
                    mPrecision = bmodel.configurationMasterHelper.VALUE_PRECISION_COUNT;
                }

                return SDUtil.format(value,
                        mPrecision,
                        bmodel.configurationMasterHelper.VALUE_COMMA_COUNT, bmodel.configurationMasterHelper.IS_DOT_FOR_GROUP);

            }
        } catch (Exception ex) {
            Commons.printException(ex);
        }

        return formattedValue;
    }

    /**
     * read text from given file and convert to string object
     * and store in object
     *
     * @param fileName
     */
    public void readBuilder(String fileName, String folder) {
        String path = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + folder + "/";
        File file = new File(path + fileName);
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));


            String st;
            while ((st = br.readLine()) != null) {
                sb.append(st);
                sb.append("\n");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        setInvoiceData(sb);

    }


    public String getPromoType(Context context, ProductMasterBO productMasterBO) {

        if (productMasterBO.getNetValue() == 0) {
            return context.getResources().getString(R.string.free);
        } else if (productMasterBO.getNetValue() < productMasterBO.getLineValue()) {
            return context.getResources().getString(R.string.net_price);
        }
        return "";

    }

    /**
     * @param productBo
     * @return uom wise qty with uom name
     */
    private String getUomWiseQty(ProductMasterBO productBo) {
        String qty = "";
        int pcQty = productBo.getOrderedPcsQty();
        int csQty = productBo.getOrderedCaseQty();
        int ouQty = productBo.getOrderedOuterQty();

        for (StandardListBO uomBo : productBo.getProductWiseUomList()) {
            if (pcQty > 0
                    && uomBo.getListID().equals(productBo.getPcUomid() + ""))
                qty = pcQty + " " + uomBo.getListName();
            else if (csQty > 0
                    && uomBo.getListID().equals(productBo.getCaseUomId() + ""))
                qty = csQty + " " + uomBo.getListName();
            else if (ouQty > 0
                    && uomBo.getListID().equals(productBo.getOuUomid() + ""))
                qty = ouQty + " " + uomBo.getListName();
        }
        return qty;
    }

    private boolean isReturnDoneForProduct(ProductMasterBO productMasterBO) {
        try {
            for (SalesReturnReasonBO bo : productMasterBO.getSalesReturnReasonList()) {
                if (bo.getPieceQty() != 0 || bo.getCaseQty() != 0
                        || bo.getOuterQty() > 0)
                    return true;

            }

            if (productMasterBO.getRepPieceQty() > 0
                    || productMasterBO.getRepOuterQty() > 0 || productMasterBO.getRepCaseQty() > 0)
                return true;

        } catch (Exception ex) {
            Commons.printException(ex);
        }
        return false;
    }

    private void loadEODStock(Vector<AttributeListBO> mAttrList, StringBuilder sb, ArrayList<StockReportBO> productList
            , String product_name_single_line, boolean isSplitConfigEnable) {

        mLoadStockTotal = 0;
        mSoldStockTotal = 0;
        mFreeStockTotal = 0;
        mCurrentStockTotal = 0;
        mEmptyStockTotal = 0;
        mReturnStockTotal = 0;
        mRepStockTotal = 0;
        mNonSalableTotal = 0;

        sb.append("\n");
        String mProductValue;
        int mLengthUptoPName;

        for (StockReportBO prod : productList) {
            if (prod.getVanLoadQty() > 0 || prod.getVanLoadQty_pc() > 0
                    || prod.getVanLoadQty_cs() > 0 || prod.getVanLoadQty_ou() > 0
                    || prod.getEmptyBottleQty() > 0 || prod.getFreeIssuedQty() > 0
                    || prod.getSoldQty() > 0 || prod.getReplacementQty() > 0 || prod.getReturnQty() > 0) {
                mLengthUptoPName = 0;
                for (AttributeListBO attr : mAttrList) {

                    mProductValue = "";
                    if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_NAME)) {
                        mProductValue = (prod.getProductShortName() != null ? prod.getProductShortName() : prod.getProductName());
                    } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_CODE)) {
                        mProductValue = prod.getProductCode();
                    } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_LOADING_STOCK)) {
                        int vanloadQty = 0;
                        if (isSplitConfigEnable) {
                            vanloadQty = (prod.getVanLoadQty_cs() * prod.getCaseSize()
                                    + prod.getVanLoadQty_ou() * prod.getOuterSize()
                                    + prod.getVanLoadQty_pc());
                            mProductValue = getProductValue(prod, attr.getAttributeName());
                        } else {
                            vanloadQty = prod.getVanLoadQty();
                            mProductValue = vanloadQty + "";
                        }
                        mLoadStockTotal += vanloadQty;
                    } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_SOLD_STOCK)) {
                        if (isSplitConfigEnable) {
                            mProductValue = getProductValue(prod, attr.getAttributeName());
                        } else {
                            mProductValue = prod.getSoldQty() + "";
                        }
                        mSoldStockTotal += prod.getSoldQty();
                    } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_FREE_ISSUED_STOCK)) {
                        if (isSplitConfigEnable) {
                            mProductValue = getProductValue(prod, attr.getAttributeName());
                        } else {
                            mProductValue = prod.getFreeIssuedQty() + "";
                        }
                        mFreeStockTotal += prod.getFreeIssuedQty();
                    } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_CURRENT_STOCK)) {
                        if (isSplitConfigEnable) {
                            mProductValue = getProductValue(prod, attr.getAttributeName());
                        } else {
                            mProductValue = prod.getSih() + "";
                        }
                        mCurrentStockTotal += prod.getSih();
                    } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_EMPTY_BOTTLE_STOCK)) {
                        if (isSplitConfigEnable) {
                            mProductValue = getProductValue(prod, attr.getAttributeName());
                        } else {
                            mProductValue = prod.getEmptyBottleQty() + "";
                        }
                        mEmptyStockTotal += prod.getEmptyBottleQty();
                    } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_RETURN_STOCK)) {
                        if (isSplitConfigEnable) {
                            mProductValue = getProductValue(prod, attr.getAttributeName());
                        } else {
                            mProductValue = prod.getReturnQty() + "";
                        }
                        mReturnStockTotal += prod.getReturnQty();
                    } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_REP_STOCK)) {
                        if (isSplitConfigEnable) {
                            mProductValue = getProductValue(prod, attr.getAttributeName());
                        } else {
                            mProductValue = prod.getReplacementQty() + "";
                        }
                        mRepStockTotal += prod.getReplacementQty();
                    } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_NON_SALABLE)) {
                        if (isSplitConfigEnable) {
                            mProductValue = getProductValue(prod, attr.getAttributeName());
                        } else {
                            mProductValue = prod.getNonSalableQty() + "";
                        }
                        mNonSalableTotal += prod.getNonSalableQty();
                    }


                    if (!attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_NAME) || product_name_single_line.equalsIgnoreCase("NO")) {
                        if (mProductValue.length() > attr.getAttributeLength()) {
                            mProductValue = mProductValue.substring(0, attr.getAttributeLength() - attr.getAttributeSpecialChar().length()) + attr.getAttributeSpecialChar();
                        } else if (mProductValue.length() < attr.getAttributeLength()) {
                            int diff = attr.getAttributeLength() - mProductValue.length();

                            if (attr.getAttributePadding().equalsIgnoreCase(ALIGNMENT_RIGHT)) {
                                mProductValue = doAlign(mProductValue, ALIGNMENT_RIGHT, diff);
                            } else {
                                mProductValue = doAlign(mProductValue, ALIGNMENT_LEFT, diff);
                            }
                        }
                    }
                    mProductValue = doAlign(mProductValue, ALIGNMENT_RIGHT, attr.getAttributeSpace());

                    sb.append(mProductValue);

                    //print the text which is next to the product name into next line
                    if (product_name_single_line.equalsIgnoreCase("YES")) {
                        // If product name is single line, then second line should be printed after the first column of first line
                        // So that bottom common labels will be aligned in straight to the first column(Ex:TAG_PRODUCT_LINE_TOTAL_WITH_QTY)..
                        mLengthUptoPName = mLengthUptoPName + attr.getAttributeLength() + attr.getAttributeSpace();
                        if (firstColumnWidth == 0) {
                            firstColumnWidth = attr.getAttributeLength() + attr.getAttributeSpace();
                        }
                        if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_NAME)) {
                            sb.append("\n");
                            char emptySpace = ' ';
                            for (int sp = 0; sp < firstColumnWidth; sp++) {
                                sb.append(emptySpace);
                            }
                        }
                    }


                }
                sb.append("\n");

            }
        }
    }

    private void addProductHeaderBorder(StringBuilder sb, int product_header_border_char_length, String product_header_border_char) {
        for (int i = 0; i < product_header_border_char_length; i++) {
            sb.append(product_header_border_char);
        }
    }


    private String centerAlignment(String mAttrValue, String property_special, int property_total_length, int attr_length) {
        int startPosition = 0;
        int length = 0;
        if (bmodel.configurationMasterHelper.IS_SHOW_PRINT_LANGUAGE_THAI)
            length = getThaiFontLength(mAttrValue, (float) 5.8);
        else
            length = mAttrValue.length();

        if (length > attr_length)
            mAttrValue = mAttrValue.substring(0, attr_length - property_special.length()) + property_special;

        startPosition = (property_total_length / 2) - (length / 2);

        return doAlign(mAttrValue, ALIGNMENT_RIGHT, startPosition);
    }


    private int rightAlignment(String attr_padding, String lineValue, String mAttrValue, String property_special, int property_total_length, int attr_length) {
        int startPosition;
        if (mAttrValue.length() > attr_length)
            mAttrValue = mAttrValue.substring(0, attr_length - property_special.length()) + property_special;

        if (attr_padding.equalsIgnoreCase(ALIGNMENT_RIGHT)) {
            startPosition = property_total_length - lineValue.length() - mAttrValue.length();
        } else {
            startPosition = property_total_length - lineValue.length() - attr_length;
        }

        return startPosition;
    }

    private String leftAlignment(String attr_name, String mAttrValue, String property_special, int property_total_length, int attr_length, String product_name_single_line, String attr_padding) {
        int length = 0;
        if (bmodel.configurationMasterHelper.IS_SHOW_PRINT_LANGUAGE_THAI && !attr_name.equalsIgnoreCase("label"))
            length = getThaiFontLength(mAttrValue, (float) 5.8);
        else
            length = mAttrValue.length();

        if (length > attr_length) {
            if (!attr_name.equalsIgnoreCase(TAG_PRODUCT_NAME)
                    || (attr_name.equalsIgnoreCase(TAG_PRODUCT_NAME) && product_name_single_line.equalsIgnoreCase("NO"))) {
                mAttrValue = mAttrValue.substring(0, attr_length - property_special.length()) + property_special;
            }
        } else if (length < attr_length) {
            int diff = attr_length - length;

            if (attr_padding.equalsIgnoreCase(ALIGNMENT_RIGHT)) {
                mAttrValue = doAlign(mAttrValue, ALIGNMENT_RIGHT, diff);
            } else {
                mAttrValue = doAlign(mAttrValue, ALIGNMENT_LEFT, diff);
            }

        }
        return mAttrValue;
    }

    /**
     * This method will print UOM Names based on config EX : cases/pieces
     *
     * @param sb
     * @param property_special
     * @param property_total_length
     */
    private void addEODSubtitle(StringBuilder sb, String property_special, int property_total_length) {
        String caseOrPieceOrOuter = "";
        String slash = "";
        if (isEOD_CS) {
            caseOrPieceOrOuter = context.getString(R.string.cases_label);
            slash = "/";
        }
        if (isEOD_PC) {
            caseOrPieceOrOuter = caseOrPieceOrOuter + slash + context.getString(R.string.pieces_label);
            slash = "/";
        }
        if (isEOD_OU) {
            caseOrPieceOrOuter = caseOrPieceOrOuter + slash + context.getString(R.string.outer_label);
        }

        sb.append(centerAlignment(caseOrPieceOrOuter, property_special, property_total_length, property_total_length));
    }

    /**
     * This method will print Quantities based on config
     *
     * @param prod
     * @param attributeName
     * @return split qty based on uom wise EX: 10/15/12 10: cases,15:pieces,12:outer
     */
    private String getProductValue(StockReportBO prod, String attributeName) {
        String caseOrPieceOrOuter = "";
        String slash = "";
        if (attributeName.equalsIgnoreCase(TAG_PRODUCT_LOADING_STOCK)) {
            if (isEOD_CS) {
                caseOrPieceOrOuter += prod.getVanLoadQty_cs() + "";
                slash = "/";
            }
            if (isEOD_PC) {
                caseOrPieceOrOuter += slash + String.valueOf(prod.getVanLoadQty_pc());
                slash = "/";
            }
            if (isEOD_OU) {
                caseOrPieceOrOuter += slash + String.valueOf(prod.getVanLoadQty_ou());
            }
        } else if (attributeName.equalsIgnoreCase(TAG_PRODUCT_SOLD_STOCK)) {

            if (isEOD_CS) {
                caseOrPieceOrOuter += prod.getSoldQty_cs() + "";
                slash = "/";
            }
            if (isEOD_PC) {
                caseOrPieceOrOuter += slash + String.valueOf(prod.getSoldQty_pc());
                slash = "/";
            }
            if (isEOD_OU) {
                caseOrPieceOrOuter += slash + String.valueOf(prod.getSoldQty_ou());
            }

        } else if (attributeName.equalsIgnoreCase(TAG_PRODUCT_FREE_ISSUED_STOCK)) {
            if (isEOD_CS) {
                caseOrPieceOrOuter += prod.getFreeIssuedQty_cs() + "";
                slash = "/";
            }
            if (isEOD_PC) {
                caseOrPieceOrOuter += slash + String.valueOf(prod.getFreeIssuedQty_pc());
                slash = "/";
            }
            if (isEOD_OU) {
                caseOrPieceOrOuter += slash + String.valueOf(prod.getFreeIssuedQty_ou());
            }

        } else if (attributeName.equalsIgnoreCase(TAG_PRODUCT_CURRENT_STOCK)) {
            if (isEOD_CS) {
                caseOrPieceOrOuter += prod.getSih_cs() + "";
                slash = "/";
            }
            if (isEOD_PC) {
                caseOrPieceOrOuter += slash + String.valueOf(prod.getSih_pc());
                slash = "/";
            }
            if (isEOD_OU) {
                caseOrPieceOrOuter += slash + String.valueOf(prod.getSih_ou());
            }

        } else if (attributeName.equalsIgnoreCase(TAG_PRODUCT_EMPTY_BOTTLE_STOCK)) {
            if (isEOD_CS) {
                caseOrPieceOrOuter += prod.getEmptyBottleQty_cs() + "";
                slash = "/";
            }
            if (isEOD_PC) {
                caseOrPieceOrOuter += slash + String.valueOf(prod.getEmptyBottleQty_pc());
                slash = "/";
            }
            if (isEOD_OU) {
                caseOrPieceOrOuter += slash + String.valueOf(prod.getEmptyBottleQty_ou());
            }

        } else if (attributeName.equalsIgnoreCase(TAG_PRODUCT_RETURN_STOCK)) {
            if (isEOD_CS) {
                caseOrPieceOrOuter += prod.getReturnQty_cs() + "";
                slash = "/";
            }
            if (isEOD_PC) {
                caseOrPieceOrOuter += slash + String.valueOf(prod.getReturnQty_pc());
                slash = "/";
            }
            if (isEOD_OU) {
                caseOrPieceOrOuter += slash + String.valueOf(prod.getReturnQty_ou());
            }

        } else if (attributeName.equalsIgnoreCase(TAG_PRODUCT_REP_STOCK)) {
            if (isEOD_CS) {
                caseOrPieceOrOuter += prod.getReplacementQty_cs() + "";
                slash = "/";
            }
            if (isEOD_PC) {
                caseOrPieceOrOuter += slash + String.valueOf(prod.getReplacementQty_pc());
                slash = "/";
            }
            if (isEOD_OU) {
                caseOrPieceOrOuter += slash + String.valueOf(prod.getReplacemnetQty_ou());
            }

        } else if (attributeName.equalsIgnoreCase(TAG_PRODUCT_NON_SALABLE)) {
            if (isEOD_CS) {
                caseOrPieceOrOuter += prod.getNonsalableQty_cs() + "";
                slash = "/";
            }
            if (isEOD_PC) {
                caseOrPieceOrOuter += slash + String.valueOf(prod.getNonsalableQty_pc());
                slash = "/";
            }
            if (isEOD_OU) {
                caseOrPieceOrOuter += slash + String.valueOf(prod.getNonsalableQty_ou());
            }
        }

        return caseOrPieceOrOuter;
    }


}
