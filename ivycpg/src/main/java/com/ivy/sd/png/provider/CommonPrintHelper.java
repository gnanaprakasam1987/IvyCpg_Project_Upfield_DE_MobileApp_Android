package com.ivy.sd.png.provider;

import android.content.Context;
import android.os.Environment;
import android.util.SparseArray;

import com.ivy.sd.png.bo.BomRetunBo;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.SchemeBO;
import com.ivy.sd.png.bo.SchemeProductBO;
import com.ivy.sd.png.bo.StoreWsieDiscountBO;
import com.ivy.sd.png.bo.TaxBO;
import com.ivy.sd.png.commons.NumberToWord;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DateUtil;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
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

    private static String TAG_RETAILER_NAME = "ret_name";
    private static String TAG_RETAILER_CODE = "ret_code";
    private static String TAG_RETAILER_ADDRESS1 = "ret_address1";
    private static String TAG_RETAILER_ADDRESS2 = "ret_address2";
    private static String TAG_RETAILER_ADDRESS3 = "ret_address3";
    private static String TAG_RETAILER_CONTACT_NUMBER = "ret_number";
    private static String TAG_RETAILER_TIN_NUMBER = "ret_tin";
    private static String TAG_RETAILER_CST_NUMBER = "ret_cst";
    private static String TAG_RETAILER_GST_NUMBER = "ret_gst_no";
    private static String TAG_RETAILER_ROUTE = "ret_route";

    private static String TAG_SELLER_NAME = "seller_name";
    private static String TAG_SELLER_ID = "seller_id";

    private static String TAG_PRODUCT_CODE = "prod_code";
    private static String TAG_PRODUCT_NAME = "prod_name";

    private static String TAG_PRODUCT_PRICE_CASE = "prod_price_case";
    private static String TAG_PRODUCT_PRICE_OUTER = "prod_price_outer";
    private static String TAG_PRODUCT_PRICE_PIECE = "prod_price_piece";

    private static String TAG_PRODUCT_MRP = "prod_mrp";

    private static String TAG_PRODUCT_QTY_CASE = "prod_qty_case";
    private static String TAG_PRODUCT_QTY_OUTER = "prod_qty_outer";
    private static String TAG_PRODUCT_QTY_PIECE = "prod_qty_piece";
    private static String TAG_PRODUCT_QTY_TOTAL_IN_PIECE = "prod_qty_total_piece";
    private static String TAG_PRODUCT_SCHEME_DISCOUNT = "prod_scheme_discount";

    private static String TAG_PRODUCT_LINE_VALUE = "prod_line_value";
    private static String TAG_PRODUCT_LINE_VALUE_EXCLUDING_TAX = "prod_line_value_excl_tax";
    private static String TAG_PRODUCT_lINE_VALUE_INCLUDING_TAX = "prod_line_value_incl_tax";

    private static String TAG_PRODUCT_TAG_DESC = "prod_tag_desc";

    private int mProductCaseQtyTotal;
    private int mProductPieceQtyTotal;
    private int mProductOuterQtyTotal;
    private int mProductQtyInPieceTotal;
    private double mProductLineValueTotal;
    private double mProductLineValueIncludingTaxTotal;
    private double mProductLineValueExcludingTaxTotal;

    private static String TAG_PRODUCT_REPLACE_QTY_PIECE = "prod_qty_replace_piece";
    private static String TAG_PRODUCT_SUM_QTY_PIECE_WITH_REP = "prod_qty_total_piece_with_rep";

    private static String TAG_PRODUCT_LINE_TOTAL = "line_total";
    private static String TAG_PRODUCT_LINE_TOTAL_WITH_QTY = "line_total_with_qty";


    private static String TAG_DISCOUNT_PRODUCT_PRICE_OFF = "discount_product_price_off";
    private static String TAG_DISCOUNT_PRODUCT_APPLY = "discount_product_apply";
    private static String TAG_DISCOUNT_PRODUCT_ENTRY = "discount_product_entry";
    private static String TAG_DISCOUNT_BILL_ENTRY = "discount_bill_entry";

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

    private HashMap<String, String> mKeyValues;

    private double totalPriceOffValue = 0;
    private ArrayList<ProductMasterBO> mOrderedProductList;

    private double total_line_value_incl_tax = 0;
    private double mBillLevelDiscountValue = 0;
    private double mBillLevelTaxValue = 0;
    private double mEmptyTotalValue = 0;
    private double total_net_payable = 0;
    public int width_image = 100;
    public int height_image = 100;
    private double mSchemeValueByAmountType = 0;
    private double netSchemeAmount = 0;

    private CommonPrintHelper(Context context) {
        this.context = context;
        this.bmodel = (BusinessModel) context;
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

    public boolean isLogoEnabled;

    private Vector<AttributeListBO> mAttributeList;

    /**
     * Read the tag from xml file and prepare print string from objects
     * if isFromAsset is true, then pass file name
     * if isFromAsset is true, then pass full path of file
     *
     * @param fileNameWithPath
     * @param isFromAsset
     */
    public void xmlRead(String fileNameWithPath, boolean isFromAsset, Vector<ProductMasterBO> productList, HashMap<String, String> keyValues) {
        try {

            resetValues();

            mKeyValues = keyValues;


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
            mAttributeList = null;

            int property_total_length = 0;
            String property_special = "";
            String lineValue = "";

            String product_name_single_line = "";

            int product_header_border_char_length = 0;
            String product_header_border_char = "-";

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
                                product_header_border_char_length = product_header_border_string == null ? property_total_length : Integer.parseInt(product_header_border_string);

                                product_header_border_char = xmlParser.getAttributeValue(null, "text");
                                product_header_border_char = product_header_border_char == null ? "-" : product_header_border_char;

                                mAttributeList = new Vector<>();
                                if (group_name.equals("product_details")) {
                                    for (int i = 0; i < product_header_border_char_length; i++) {
                                        sb.append(product_header_border_char);
                                    }
                                    sb.append(newline);

                                }
                            }

                            lineValue = "";
                        } else if (name.equals("view")) {
                            String attr_name = xmlParser.getAttributeValue(null, "name");
                            int attr_length = Integer.parseInt(xmlParser.getAttributeValue(null, "length"));

                            String attr_text = xmlParser.getAttributeValue(null, "text");
                            attr_text = attr_text == null ? "" : attr_text;

                            String attr_secondary_text = xmlParser.getAttributeValue(null, "secondarytext");
                            attr_secondary_text = attr_secondary_text == null ? "" : attr_secondary_text;

                            String attr_padding = xmlParser.getAttributeValue(null, "padding");
                            attr_padding = attr_padding == null ? "left" : attr_padding;

                            String attr_space_str = xmlParser.getAttributeValue(null, "space");
                            int attr_space = attr_space_str == null ? 0 : Integer.parseInt(attr_space_str);

                            String attr_precision_str = xmlParser.getAttributeValue(null, "precision_count");
                            int attr_precision = attr_precision_str == null ? -1 : Integer.parseInt(attr_precision_str);

                            String attr_align = xmlParser.getAttributeValue(null, "align");
                            attr_align = attr_align == null ? "left" : attr_align;

                            String attr_bold = xmlParser.getAttributeValue(null, "bold");
                            attr_bold = attr_bold == null ? "No" : attr_bold;

                            String attr_repeat = xmlParser.getAttributeValue(null, "repeat");
                            attr_repeat = attr_repeat == null ? "No" : attr_repeat;

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
                                mAttrValue = getValue(attr_name, attr_text, attr_secondary_text, attr_precision);

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
                                    && !attr_name.contains("net_amount")
                                    && !attr_name.contains("net_scheme_discount")
                                    && !attr_name.contains("amount_word")) {
                                if (attr_align.equalsIgnoreCase(ALIGNMENT_LEFT)) {
                                    if (mAttrValue.length() > attr_length) {
                                        mAttrValue = mAttrValue.substring(0, attr_length - property_special.length()) + property_special;
                                    } else if (mAttrValue.length() < attr_length) {
                                        int diff = attr_length - mAttrValue.length();

                                        if (attr_padding.equalsIgnoreCase(ALIGNMENT_RIGHT)) {
                                            mAttrValue = doAlign(mAttrValue, ALIGNMENT_RIGHT, diff);
                                        } else {
                                            mAttrValue = doAlign(mAttrValue, ALIGNMENT_LEFT, diff);
                                        }

                                    }
                                    mAttrValue = doAlign(mAttrValue, ALIGNMENT_RIGHT, attr_space);

                                } else if (attr_align.equalsIgnoreCase(ALIGNMENT_RIGHT)) {
                                    int startPosition;

                                    if (mAttrValue.length() > attr_length)
                                        mAttrValue = mAttrValue.substring(0, attr_length - property_special.length()) + property_special;

                                    if (attr_padding.equalsIgnoreCase(ALIGNMENT_RIGHT)) {
                                        startPosition = property_total_length - lineValue.length() - mAttrValue.length();
                                    } else {
                                        startPosition = property_total_length - lineValue.length() - attr_length;
                                    }
                                    mAttrValue = doAlign(mAttrValue, ALIGNMENT_RIGHT, startPosition);

                                } else if (attr_align.equalsIgnoreCase(ALIGNMENT_CENTER)) {
                                    int startPosition;

                                    if (mAttrValue.length() > attr_length)
                                        mAttrValue = mAttrValue.substring(0, attr_length - property_special.length()) + property_special;

                                    startPosition = (property_total_length / 2) - (mAttrValue.length() / 2);

                                    mAttrValue = doAlign(mAttrValue, ALIGNMENT_RIGHT, startPosition);
                                }
                            }
                            if (attr_bold.equalsIgnoreCase("YES")) {
                                mAttrValue = "#B#" + mAttrValue;
                            }

                            sb.append(mAttrValue);
                            lineValue += mAttrValue;

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
                            property_total_length = Integer.parseInt(xmlParser.getAttributeValue(null, "total_length"));
                            mPaperLenghtInChar = property_total_length;
                            property_special = xmlParser.getAttributeValue(null, "special");
                            property_special = property_special == null ? "" : property_special;
                            String pres_str = xmlParser.getAttributeValue(null, "precision_count");
                            mGlobalPrecision = pres_str == null ? -1 : Integer.parseInt(pres_str);
                        } else if (name.equalsIgnoreCase("logo")) {
                            isLogoEnabled = true;
                        } else if (name.equalsIgnoreCase("newline")) {
                            String attr_count_str = xmlParser.getAttributeValue(null, "count");
                            int attr_count = attr_count_str == null ? 1 : Integer.parseInt(attr_count_str);
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

                                getBillLevelDiscount();
                                getBillLevelTaxValue();
                                getEmptyReturnValue();
                                total_net_payable = total_line_value_incl_tax - mBillLevelDiscountValue + mBillLevelTaxValue + mEmptyTotalValue;
                            } else if (group_name != null && group_name.equalsIgnoreCase("empty_return")) {
                                printEmptyReturn(mAttributeList, sb);
                            }
                            group_name = "";
                            product_bacth = "NO";
                            product_free_product = "NO";
                            sb.append(newline);

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
    private String getValue(String tag, String label, String mSecondaryLabel, int precisionCount) {
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
            value = label + DateUtil.convertFromServerDateToRequestedFormat(SDUtil.now(SDUtil.DATE_GLOBAL),
                    bmodel.configurationMasterHelper.outDateFormat);
        } else if (tag.equalsIgnoreCase(TAG_TIME)) {
            value = label + SDUtil.now(SDUtil.TIME);
        } else if (tag.equalsIgnoreCase(TAG_DELIVERY_DATE)) {
            String deliveryDate = bmodel.getDeliveryDate(bmodel.getRetailerMasterBO().getRetailerID());
            String delDate = DateUtil.convertFromServerDateToRequestedFormat(deliveryDate, bmodel.configurationMasterHelper.outDateFormat);
            value = label + delDate;
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
        } else if (tag.equalsIgnoreCase(TAG_RETAILER_NAME)) {
            value = label + bmodel.getRetailerMasterBO().getRetailerName();
        } else if (tag.equalsIgnoreCase(TAG_RETAILER_CODE)) {
            value = label + bmodel.getRetailerMasterBO().getRetailerCode();
        } else if (tag.equalsIgnoreCase(TAG_RETAILER_ADDRESS1)) {
            value = label + bmodel.getRetailerMasterBO().getAddress1();
        } else if (tag.equalsIgnoreCase(TAG_RETAILER_ADDRESS2)) {
            value = label + bmodel.getRetailerMasterBO().getAddress2();
        } else if (tag.equalsIgnoreCase(TAG_RETAILER_ADDRESS3)) {
            value = label + bmodel.getRetailerMasterBO().getAddress3();
        } else if (tag.equalsIgnoreCase(TAG_RETAILER_CONTACT_NUMBER)) {
            value = label + bmodel.getRetailerMasterBO().getContactnumber();
        } else if (tag.equalsIgnoreCase(TAG_RETAILER_TIN_NUMBER)) {
            value = label + bmodel.getRetailerMasterBO().getTinnumber();
        } else if (tag.equalsIgnoreCase(TAG_RETAILER_CST_NUMBER)) {
            value = label + bmodel.getRetailerMasterBO().getCredit_invoice_count();
        } else if (tag.equalsIgnoreCase(TAG_RETAILER_GST_NUMBER)) {
            value = label + bmodel.getRetailerMasterBO().getGSTNumber();
        } else if (tag.equalsIgnoreCase(TAG_SELLER_NAME)) {
            value = label + bmodel.userMasterHelper.getUserMasterBO().getUserName();
        } else if (tag.equalsIgnoreCase(TAG_SELLER_ID)) {
            value = label + bmodel.userMasterHelper.getUserMasterBO().getUserid();
        } else if (tag.equalsIgnoreCase(TAG_DISCOUNT_PRODUCT_PRICE_OFF)) {
            value = alignWithLabelForSingleLine(label, formatValueInPrint(totalPriceOffValue, precisionCount));
        } else if (tag.equalsIgnoreCase(TAG_DISCOUNT_PRODUCT_APPLY)) {
            value = getProductLevelApplyDiscount(precisionCount);
        } else if (tag.equalsIgnoreCase(TAG_DISCOUNT_PRODUCT_ENTRY)) {
            value = alignWithLabelForSingleLine(label, formatValueInPrint(bmodel.productHelper.updateProductDiscountUsingEntry(mOrderedProductList), precisionCount));
        } else if (tag.equalsIgnoreCase(TAG_TAX_PRODUCT)) {
            value = PrepareProductLevelTax(precisionCount);
        } else if (tag.equalsIgnoreCase(TAG_DISCOUNT_BILL_ENTRY)) {
            value = alignWithLabelForSingleLine(label, formatValueInPrint(mBillLevelDiscountValue, precisionCount) + "");
        } else if (tag.equalsIgnoreCase(TAG_TAX_BILL)) {
            value = printBillLevelTax(precisionCount);
        } else if (tag.equalsIgnoreCase(TAG_PRODUCT_LINE_TOTAL)) {
            value = alignWithLabelForSingleLine(label, formatValueInPrint(total_line_value_incl_tax, precisionCount));
        } else if (tag.equalsIgnoreCase(TAG_PRODUCT_LINE_TOTAL_WITH_QTY)) {
            value = getTotalWithQty(label);
        } else if (tag.equalsIgnoreCase(TAG_NET_PAYABLE)) {
            value = alignWithLabelForSingleLine(label, formatSalesValueInPrint(total_net_payable, precisionCount));
        } else if (tag.equalsIgnoreCase(TAG_NET_PAYABLE_IN_WORDS)) {
            value = label + getAmountInWords(formatSalesValueInPrint(total_net_payable, precisionCount).replace(",", ""));
        } else if (tag.equalsIgnoreCase("empty_total")) {
            value = alignWithLabel(label, formatValueInPrint(mEmptyTotalValue, precisionCount));
        } else if (tag.equalsIgnoreCase(TAG_KEY1)) {
            if (mKeyValues != null)
                value = label + mKeyValues.get(TAG_KEY1);
        } else if (tag.equalsIgnoreCase(TAG_KEY2)) {
            if (mKeyValues != null)
                value = label + mKeyValues.get(TAG_KEY2);
        } else if (tag.equalsIgnoreCase(TAG_ORDER_NUMBER)) {
            value = label + bmodel.getOrderid().replaceAll("\'", "");
        } else if (tag.equalsIgnoreCase(TAG_RETAILER_ROUTE)) {
            value = label + bmodel.beatMasterHealper.getBeatMasterBOByID(bmodel.getRetailerMasterBO().getBeatID());
        } else if (tag.equalsIgnoreCase(TAG_NET_SCHEME_DISCOUNT)) {
            value = alignWithLabelForSingleLine(label, formatValueInPrint(netSchemeAmount, precisionCount));
        }


        return value;
    }

    private String getTotalWithQty(String label) {
        String mProductValue;
        StringBuilder sb = new StringBuilder();

        for (AttributeListBO attr : mAttributeList) {
            mProductValue = "";
            if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_NAME)) {
                mProductValue = label;
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

            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_SUM_QTY_PIECE_WITH_REP)) {

            }


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

            mProductValue = doAlign(mProductValue, ALIGNMENT_RIGHT, attr.getAttributeSpace());

            sb.append(mProductValue);
        }
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

        ProductMasterBO productBO;
        for (int i = 0; i < productsCount; i++) {
            productBO = productList.elementAt(i);
            if (productBO.getOrderedCaseQty() > 0
                    || productBO.getOrderedPcsQty() > 0
                    || productBO.getOrderedOuterQty() > 0
                    ) {
                mOrderedProductList.add(productBO);
            } else {
                // Adding replaced qty
                if (productBO.getRepOuterQty() > 0 || productBO.getRepCaseQty() > 0 || productBO.getRepPieceQty() > 0) {
                    mOrderedProductList.add(productBO);
                }
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
                } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_QTY_TOTAL_IN_PIECE)) {
                    mProductValue = (prod.getOrderedPcsQty()
                            + (prod.getOrderedCaseQty() * prod.getCaseSize())
                            + (prod.getOrderedOuterQty() * prod.getOutersize())) + "";
                    mProductQtyInPieceTotal = mProductQtyInPieceTotal + Integer.parseInt(mProductValue);
                } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_LINE_VALUE)) {
                    double lineValue = (prod.getOrderedOuterQty() * prod.getOsrp())
                            + (prod.getOrderedCaseQty() * prod.getCsrp())
                            + (prod.getOrderedPcsQty() * prod.getSrp());
                    mProductValue = formatValueInPrint(lineValue, attr.getmAttributePrecision());
                    mProductLineValueTotal = mProductLineValueTotal + Double.parseDouble(mProductValue);
                } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_LINE_VALUE_EXCLUDING_TAX)) {
                    mProductValue = formatValueInPrint(prod.getTaxValue() > 0 ? prod.getTaxValue() : prod.getDiscount_order_value(), attr.getmAttributePrecision());
                    mProductLineValueExcludingTaxTotal = mProductLineValueExcludingTaxTotal + (prod.getTaxValue() > 0 ? prod.getTaxValue() : prod.getDiscount_order_value());
                } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_lINE_VALUE_INCLUDING_TAX)) {
                    mProductValue = formatValueInPrint(prod.getDiscount_order_value(), attr.getmAttributePrecision());
                    mProductLineValueIncludingTaxTotal = mProductLineValueIncludingTaxTotal + prod.getDiscount_order_value();
                } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_REPLACE_QTY_PIECE)) {
                    mProductValue = prod.getRepPieceQty() + (prod.getRepCaseQty() * prod.getCaseSize()) + (prod.getRepOuterQty() * prod.getOutersize()) + "";
                } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_SUM_QTY_PIECE_WITH_REP)) {
                    mProductValue = prod.getRepPieceQty()
                            + (prod.getRepCaseQty() * prod.getCaseSize())
                            + (prod.getRepOuterQty() * prod.getOutersize())
                            + (prod.getOrderedPcsQty()
                            + (prod.getOrderedCaseQty() * prod.getCaseSize())
                            + (prod.getOrderedOuterQty() * prod.getOutersize())) + "";
                } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_TAG_DESC)) {
                    mProductValue = prod.getDescription() + "";
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

            //load the ordered product line item - end

            total_line_value_incl_tax = total_line_value_incl_tax + SDUtil.convertToDouble(SDUtil.format(prod.getDiscount_order_value(), 2, 0));

            int totalProductQty = prod.getOrderedPcsQty() + prod.getOrderedCaseQty() * prod.getCaseSize()
                    + prod.getOrderedOuterQty() * prod.getOutersize();
            totalPriceOffValue = totalPriceOffValue + totalProductQty * prod.getPriceoffvalue();

            //load scheme free product
            if (isLoadSchemeFreeProduct.equalsIgnoreCase("YES")) {
                loadSchemeFreeProduct(prod, mAttrList, sb, product_name_single_line);
            }
        }
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
            if (productBO.getOrderedCaseQty() > 0
                    || productBO.getOrderedPcsQty() > 0
                    || productBO.getOrderedOuterQty() > 0) {

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

        for (ProductMasterBO prod : mOrderedProductList) {

            if (prod.getBatchwiseProductCount() > 0
                    && (batchList = bmodel.batchAllocationHelper.getBatchlistByProductID().get(prod.getProductID())) != null) {

                for (ProductMasterBO batchProductBO : batchList) {
                    mLengthUptoPName = 0;
                    int totalBatchQty = batchProductBO.getOrderedPcsQty() + batchProductBO.getOrderedCaseQty() * prod.getCaseSize()
                            + batchProductBO.getOrderedOuterQty() * prod.getOutersize();
                    if (totalBatchQty > 0) {
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
                            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_QTY_TOTAL_IN_PIECE)) {
                                mProductValue = (batchProductBO.getOrderedPcsQty()
                                        + (batchProductBO.getOrderedCaseQty() * prod.getCaseSize())
                                        + (batchProductBO.getOrderedOuterQty() * prod.getOutersize())) + "";
                                mProductQtyInPieceTotal = mProductQtyInPieceTotal + Integer.parseInt(mProductValue);
                            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_LINE_VALUE)) {
                                double lineValue = (batchProductBO.getOrderedOuterQty() * batchProductBO.getOsrp())
                                        + (batchProductBO.getOrderedCaseQty() * batchProductBO.getCsrp())
                                        + (batchProductBO.getOrderedPcsQty() * batchProductBO.getSrp());
                                mProductValue = formatValueInPrint(lineValue, attr.getmAttributePrecision());
                                mProductLineValueTotal = mProductLineValueTotal + Double.parseDouble(mProductValue);
                            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_LINE_VALUE_EXCLUDING_TAX)) {
                                mProductValue = formatValueInPrint(batchProductBO.getTaxValue() > 0 ? batchProductBO.getTaxValue() : batchProductBO.getDiscount_order_value(), attr.getmAttributePrecision());
                                mProductLineValueExcludingTaxTotal = mProductLineValueExcludingTaxTotal + Double.parseDouble(mProductValue);
                            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_lINE_VALUE_INCLUDING_TAX)) {
                                mProductValue = formatValueInPrint(batchProductBO.getDiscount_order_value(), attr.getmAttributePrecision());
                                mProductLineValueIncludingTaxTotal = mProductLineValueIncludingTaxTotal + Double.parseDouble(mProductValue);
                            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_REPLACE_QTY_PIECE)) {
                                mProductValue = batchProductBO.getRepPieceQty() + (batchProductBO.getRepCaseQty() * batchProductBO.getCaseSize()) + (batchProductBO.getRepOuterQty() * batchProductBO.getOutersize()) + "";
                            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_SUM_QTY_PIECE_WITH_REP)) {
                                mProductValue = batchProductBO.getRepPieceQty() + (batchProductBO.getRepCaseQty() * batchProductBO.getCaseSize()) + (batchProductBO.getRepOuterQty() * batchProductBO.getOutersize()) + (batchProductBO.getOrderedPcsQty() + (batchProductBO.getOrderedCaseQty() * batchProductBO.getCaseSize()) + (batchProductBO.getOrderedOuterQty() * batchProductBO.getOutersize())) + "";
                            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_TAG_DESC)) {
                                mProductValue = prod.getDescription() + "";
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
                                SDUtil.convertToDouble(SDUtil.format(batchProductBO.getDiscount_order_value(), 2, 0));

                        totalPriceOffValue = totalPriceOffValue + (totalBatchQty * batchProductBO.getPriceoffvalue());
                    }
                }//

                //load scheme free product
                if (isLoadSchemeFreeProduct.equalsIgnoreCase("YES")) {
                    loadSchemeFreeProduct(prod, mAttrList, sb, product_name_single_line);
                }

            }
        }

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
                                }
                            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_QTY_PIECE)) {
                                if (freeProduct.getPcUomid() == schemeProductBO.getUomID()
                                        && freeProduct.getPcUomid() != 0) {
                                    mProductValue = schemeProductBO.getQuantitySelected() + "";
                                }
                            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_QTY_OUTER)) {
                                if (freeProduct.getOuUomid() == schemeProductBO.getUomID()
                                        && freeProduct.getOuUomid() != 0) {
                                    mProductValue = schemeProductBO.getQuantitySelected() + "";
                                }
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

                            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_LINE_VALUE)) {
                                mProductValue = formatValueInPrint(schemeProductBO.getLineValue(), attr.getmAttributePrecision());
                            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_LINE_VALUE_EXCLUDING_TAX)) {
                                mProductValue = formatValueInPrint((schemeProductBO.getLineValue() - schemeProductBO.getTaxAmount()), attr.getmAttributePrecision());
                            } else if (attr.getAttributeName().equalsIgnoreCase(TAG_PRODUCT_lINE_VALUE_INCLUDING_TAX)) {
                                mProductValue = formatValueInPrint(schemeProductBO.getLineValue(), attr.getmAttributePrecision());
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

    private void calculateSchemeAmountDiscountValue() {

        ArrayList<SchemeBO> appliedSchemeList = bmodel.schemeDetailsMasterHelper
                .getAppliedSchemeList();
        double mBuyProdDiscountedValue = 0;
        if (appliedSchemeList != null) {
            for (SchemeBO schemeBO : appliedSchemeList) {
                if (schemeBO != null) {

                    if (schemeBO.isAmountTypeSelected()) {

                        mSchemeValueByAmountType = mSchemeValueByAmountType + schemeBO.getSelectedAmount();
                        netSchemeAmount += schemeBO.getSelectedAmount();
                    } else if (schemeBO.isDiscountPrecentSelected() || schemeBO.isPriceTypeSeleted()) {
                        for (SchemeProductBO buyProd : schemeBO.getBuyingProducts()) {
                            ProductMasterBO productBO = bmodel.productHelper
                                    .getProductMasterBOById(buyProd
                                            .getProductId());
                            if (productBO != null) {
                                if (productBO.getOrderedPcsQty() > 0
                                        || productBO.getOrderedCaseQty() > 0
                                        || productBO.getOrderedOuterQty() > 0) {
                                    mBuyProdDiscountedValue = mBuyProdDiscountedValue + buyProd.getDiscountValue();
                                }
                            }
                        }

                        netSchemeAmount += schemeBO.getSelectedAmount();
                    }


                }
            }

            total_line_value_incl_tax = total_line_value_incl_tax - mSchemeValueByAmountType;
        }
    }

    private void loadSchemeDiscount(Vector<AttributeListBO> mAttrList, String product_name_single_line, StringBuilder sb) {
        int mLengthUptoPName;

        ArrayList<SchemeBO> appliedSchemeList = bmodel.schemeDetailsMasterHelper
                .getAppliedSchemeList();
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
                        mSchemeValueByAmountType = mSchemeValueByAmountType + schemeValue;
                        netSchemeAmount += schemeValue;
                    } else if (schemeBO.isDiscountPrecentSelected() || schemeBO.isPriceTypeSeleted()) {
                        schemeName = schemeBO.getProductName();
                        for (SchemeProductBO buyProd : schemeBO.getBuyingProducts()) {
                            ProductMasterBO productBO = bmodel.productHelper
                                    .getProductMasterBOById(buyProd
                                            .getProductId());
                            if (productBO != null) {
                                if (productBO.getOrderedPcsQty() > 0
                                        || productBO.getOrderedCaseQty() > 0
                                        || productBO.getOrderedOuterQty() > 0) {
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

            total_line_value_incl_tax = total_line_value_incl_tax - mSchemeValueByAmountType;
        }
    }

    /**
     * Load product level discount applied from master
     *
     * @return
     */
    private String getProductLevelApplyDiscount(int precision) {
        StringBuffer sb = new StringBuffer();
        ArrayList<Integer> mDiscountTypeIdList = bmodel.productHelper.getTypeIdList();
        SparseArray<ArrayList<Integer>> mDiscountIdListByTypeId = bmodel.productHelper.getDiscountIdListByTypeId();
        if (mDiscountTypeIdList != null && mDiscountIdListByTypeId != null) {
            for (Integer typeId : mDiscountTypeIdList) {
                ArrayList<Integer> discountIdList = mDiscountIdListByTypeId.get(typeId);
                if (discountIdList != null) {
                    String discountDescription = "";
                    double totalDiscountValue = 0;
                    for (int discountid : discountIdList) {
                        ArrayList<StoreWsieDiscountBO> discountList = bmodel.productHelper.getProductDiscountListByDiscountID().get(discountid);
                        if (discountList != null) {
                            for (StoreWsieDiscountBO storeWsieDiscountBO : discountList) {
                                discountDescription = storeWsieDiscountBO.getDescription();
                                ProductMasterBO productMasterBO = bmodel.productHelper.getProductMasterBOById(storeWsieDiscountBO.getProductId() + "");
                                if (productMasterBO != null) {
                                    int totalProductQty = 0;
                                    totalProductQty = productMasterBO.getOrderedPcsQty()
                                            + productMasterBO.getOrderedCaseQty() * productMasterBO.getCaseSize()
                                            + productMasterBO.getOrderedOuterQty() * productMasterBO.getOutersize();
                                    if (totalProductQty > 0) {
                                        if (productMasterBO.getBatchwiseProductCount() > 0) {

                                            ArrayList<ProductMasterBO> batchList = bmodel.batchAllocationHelper.getBatchlistByProductID().get(productMasterBO.getProductID());
                                            if (batchList != null) {
                                                for (ProductMasterBO batchProductBO : batchList) {
                                                    double totalValue = 0;
                                                    double batchDiscountValue = 0;
                                                    int totalBatchQty = batchProductBO.getOrderedPcsQty() + batchProductBO.getOrderedCaseQty() * productMasterBO.getCaseSize()
                                                            + batchProductBO.getOrderedOuterQty() * productMasterBO.getOutersize();

                                                    if (batchProductBO.getSchemeAppliedValue() > 0) {
                                                        totalValue = batchProductBO.getSchemeAppliedValue();
                                                    } else {
                                                        totalValue = batchProductBO.getOrderedPcsQty()
                                                                * batchProductBO.getSrp()
                                                                + batchProductBO.getOrderedCaseQty()
                                                                * batchProductBO.getCsrp()
                                                                + batchProductBO.getOrderedOuterQty()
                                                                * batchProductBO.getOsrp();
                                                    }

                                                    if (storeWsieDiscountBO.getIsPercentage() == 1) {
                                                        batchDiscountValue = totalValue * storeWsieDiscountBO.getDiscount() / 100;


                                                    } else if (storeWsieDiscountBO.getIsPercentage() == 0) {
                                                        batchDiscountValue = totalBatchQty * storeWsieDiscountBO.getDiscount();
                                                    }

                                                    totalDiscountValue = totalDiscountValue + batchDiscountValue;
                                                }
                                            }
                                        } else {
                                            double totalValue = 0;
                                            double productDiscount = 0;

                                            if (productMasterBO.getSchemeAppliedValue() > 0) {
                                                totalValue = productMasterBO.getSchemeAppliedValue();
                                            } else {
                                                totalValue = productMasterBO.getOrderedPcsQty() * productMasterBO.getSrp()
                                                        + productMasterBO.getOrderedCaseQty() * productMasterBO.getCsrp()
                                                        + productMasterBO.getOrderedOuterQty() * productMasterBO.getOsrp();
                                            }

                                            if (storeWsieDiscountBO.getIsPercentage() == 1) {
                                                productDiscount = totalValue * storeWsieDiscountBO.getDiscount() / 100;
                                            } else if (storeWsieDiscountBO.getIsPercentage() == 0) {
                                                productDiscount = totalProductQty * storeWsieDiscountBO.getDiscount();
                                            }

                                            totalDiscountValue = totalDiscountValue + productDiscount;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (totalDiscountValue > 0) {
                        String s = "";
                        if (discountDescription.length() < 20) {
                            s = discountDescription;
                        } else {
                            s = discountDescription.substring(0, 20);
                        }
                        s = alignWithLabelForSingleLine(s, formatValueInPrint(totalDiscountValue, precision));
                        sb.append(s);
                        sb.append("\n");
                    }
                }
            }
        }

        return sb.toString();
    }

    /**
     * load product level tax which is applied
     *
     * @return
     */

    private String PrepareProductLevelTax(int precision) {

        StringBuffer sb = new StringBuffer();

        // load tax details
        bmodel.productHelper.loadTaxDetailsForPrint(bmodel.invoiceNumber);
        // load tax product details
        bmodel.productHelper.loadTaxProductDetailsForPrint(bmodel.invoiceNumber);

        ArrayList<TaxBO> groupIdList = bmodel.productHelper.getGroupIdList();

        if (groupIdList != null) {

            SparseArray<LinkedHashSet<TaxBO>> totalTaxListByGroupId = bmodel.productHelper.getGroupDesc2ByGroupId();
            HashMap<String, HashSet<String>> productListByGroupId = bmodel.productHelper.getProductIdByTaxGroupId();
            HashMap<String, HashSet<String>> freeProductListByGroupId = bmodel.productHelper.loadTaxFreeProductDetails(bmodel.invoiceNumber);

            String taxDesc = "";
            String previousTaxDesc = "";
            String s = "";

            for (TaxBO taxBO : groupIdList) {// here group id is tax type..

                //Getting tax list by groupid, to show group(tax type) wise
                LinkedHashSet<TaxBO> totalTaxList = totalTaxListByGroupId.get(taxBO.getGroupId());


                if (totalTaxList != null) {
                    for (TaxBO totalTaxBO : totalTaxList) {


                        taxDesc = totalTaxBO.getTaxDesc2();
                        double taxpercentege = totalTaxBO.getTaxRate();

                        //Same tax type with different tax rate may mapped to a product, so getting product list with the use of groupid(taxtype) anf percentage.
                        HashSet<String> taxProductList = productListByGroupId.get(taxBO.getGroupId() + "" + taxpercentege);
                        HashSet<String> taxFreeProductList = freeProductListByGroupId.get(taxBO.getGroupId() + "" + taxpercentege);

                        double totalTax = 0.0;
                        double totalExcludeValue = 0.0;

                        if (taxProductList != null) {
                            for (String productid : taxProductList) {// normal products

                                ProductMasterBO prodcutBO = bmodel.productHelper.getProductMasterBOById(productid);
                                if (prodcutBO != null) {

                                    //Tax may be in multiple forms for product(Ex:tax on tax..), so this below loop is used to calculate values for all tax mapped to product
                                    if (bmodel.productHelper.getmTaxListByProductId().get(productid) != null) {
                                        for (TaxBO productTaxBo : bmodel.productHelper.getmTaxListByProductId().get(productid)) {

                                            if (productTaxBo.getTaxType().equals(taxBO.getGroupId() + "") && productTaxBo.getTaxRate() == taxpercentege) {

                                                totalTax += productTaxBo.getTotalTaxAmount();
                                                totalExcludeValue = totalExcludeValue + productTaxBo.getTaxableAmount();
                                            }
                                        }
                                    }


                                }
                            }
                        }

                        //Tax can be applied to Free products, so below set of code is used..
                        if (taxFreeProductList != null) {
                            for (String productid : taxFreeProductList) {// free products

                                //Tax may be in multiple forms for product(Ex:tax on tax..), so this below loop is used to calculate values for all tax mapped to product

                                if (bmodel.getmFreeProductTaxListByProductId().get(productid) != null) {
                                    for (TaxBO productTaxBo : bmodel.getmFreeProductTaxListByProductId().get(productid)) {

                                        if (productTaxBo.getTaxType().equals(taxBO.getGroupId() + "") && productTaxBo.getTaxRate() == taxpercentege) {

                                            totalTax += productTaxBo.getTotalTaxAmount();
                                            totalExcludeValue = totalExcludeValue + productTaxBo.getTaxableAmount();
                                        }
                                    }
                                }


                            }

                        }


                        // preparing string builder to show product level tax in print
                        if (taxProductList != null || taxFreeProductList != null) {
                            if (totalTax > 0) {
                                if (!taxDesc.equals(previousTaxDesc)) {
                                    if (taxDesc.length() > 10) {
                                        s = taxDesc.substring(0, 10);
                                    } else {
                                        s = taxDesc;
                                    }
                                } else {
                                    s = doAlign("", ALIGNMENT_RIGHT, taxDesc.length());
                                }

                                s = s + " " + taxpercentege + "% on Rs " + formatValueInPrint(totalExcludeValue, precision);

                                s = alignWithLabelForSingleLine(s, formatValueInPrint(totalTax, precision));

                                sb.append(s);

                                sb.append("\n");
                            }
                        }

                        previousTaxDesc = taxDesc;
                    }
                }

            }

        }
        return sb.toString();
    }

    /**
     * get bill level discount value
     */
    private void getBillLevelDiscount() {

        double discount = SDUtil.convertToDouble(bmodel.invoiceDisount);
        double discountValue = 0;

        if (bmodel.configurationMasterHelper.discountType == 1) {
            if (discount > 100)
                discount = 100;
            discountValue = ((total_line_value_incl_tax / 100) * discount);
        } else if (bmodel.configurationMasterHelper.discountType == 2) {
            discountValue = discount;
        } else {
            discountValue = discount;
        }
        mBillLevelDiscountValue = Double.parseDouble(bmodel.formatValue(discountValue));
    }

    private void getBillLevelTaxValue() {
        try {
            mBillLevelTaxValue = 0;
            final ArrayList<TaxBO> taxList = bmodel.productHelper.getTaxList();

            if (taxList != null && taxList.size() > 0) {
                if (bmodel.configurationMasterHelper.SHOW_INCLUDE_BILL_TAX) {
                    double mTotalIncludeTax = total_line_value_incl_tax - mBillLevelDiscountValue;
                    double taxValue;
                    double taxTotal = 0;
                    for (TaxBO taxBO : taxList) {
                        taxValue = mTotalIncludeTax * taxBO.getTaxRate() / 100;
                        taxValue = SDUtil.convertToDouble(SDUtil.format(taxValue, 2, 0));
                        taxTotal = taxTotal + taxValue;
                    }
                    mBillLevelTaxValue = taxTotal;
                }
            }

        } catch (Exception e) {
            Commons.printException(e);
        }

    }

    /**
     * get bill level tax
     *
     * @return
     */
    private String printBillLevelTax(int precision) {
        StringBuffer sb = new StringBuffer();
        try {
            final ArrayList<TaxBO> taxList = bmodel.productHelper.getTaxList();
            if (taxList != null && taxList.size() > 0) {
                if (bmodel.configurationMasterHelper.SHOW_INCLUDE_BILL_TAX) {
                    double mTotalIncludeTax = total_line_value_incl_tax - mBillLevelDiscountValue;
                    mTotalIncludeTax = Double.parseDouble(bmodel.formatValue(mTotalIncludeTax));

                    double taxValue;
                    double taxTotal = 0;
                    for (TaxBO taxBO : taxList) {
                        taxValue = mTotalIncludeTax * taxBO.getTaxRate() / 100;
                        taxValue = SDUtil.convertToDouble(SDUtil.format(taxValue, 2, 0));
                        taxTotal = taxTotal + taxValue;
                        sb.append(alignWithLabelForSingleLine(taxBO.getTaxDesc() + "(" + taxBO.getTaxRate() + "%) : ", taxValue + ""));
                        sb.append("\n");
                    }

                } else {

                    double taxTotal = 0;

                    for (TaxBO taxBO : taxList) {
                        taxTotal = taxTotal + taxBO.getTaxRate();
                    }

                    double mTotalIncludeTax = total_line_value_incl_tax - mBillLevelDiscountValue;
                    mTotalIncludeTax = Double.parseDouble(bmodel.formatValue(mTotalIncludeTax));

                    double mTotalExcludeTaxAmount = mTotalIncludeTax / (1 + taxTotal / 100);

                    sb.append(alignWithLabelForSingleLine("Excl Tax:", "" + formatValueInPrint(mTotalExcludeTaxAmount, precision)));

                    sb.append("\n");

                    double taxValue;
                    for (TaxBO taxBO : taxList) {
                        taxValue = mTotalExcludeTaxAmount * taxBO.getTaxRate() / 100;
                        taxValue = SDUtil.convertToDouble(SDUtil.format(taxValue, 2, 0));
                        sb.append(alignWithLabelForSingleLine(taxBO.getTaxDesc() + "(" + taxBO.getTaxRate() + ")", taxValue + ""));
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

        ArrayList<BomRetunBo> mEmptyProducts;
        double totalEmp = 0;

        if (bmodel.configurationMasterHelper.SHOW_GROUPPRODUCTRETURN)
            mEmptyProducts = bmodel.productHelper
                    .getBomReturnTypeProducts();
        else
            mEmptyProducts = bmodel.productHelper.getBomReturnProducts();

        if (mEmptyProducts != null && mEmptyProducts.size() > 0) {
            Collections.sort(mEmptyProducts, BomRetunBo.SKUWiseAscending);
            for (BomRetunBo productBO : mEmptyProducts) {
                totalEmp = (productBO.getLiableQty() * productBO.getpSrp()) - (productBO.getReturnQty() * productBO.getpSrp());
                mEmptyTotalValue = mEmptyTotalValue + totalEmp;
            }
        }
    }

    /**
     * load empty return details
     *
     * @param mAttrList - define value to be displayed
     * @param sb        - source and output
     */
    private void printEmptyReturn(Vector<AttributeListBO> mAttrList, StringBuilder sb) {

        sb.append("\n");

        ArrayList<BomRetunBo> mEmptyProducts;
        //double totalEmp = 0 , mLiableTot = 0, mReturnTot = 0;

        if (bmodel.configurationMasterHelper.SHOW_GROUPPRODUCTRETURN)
            mEmptyProducts = bmodel.productHelper
                    .getBomReturnTypeProducts();
        else
            mEmptyProducts = bmodel.productHelper.getBomReturnProducts();


        if (mEmptyProducts != null && mEmptyProducts.size() > 0) {

            //sb.append("\n");

            Collections.sort(mEmptyProducts, BomRetunBo.SKUWiseAscending);
            String mProductValue = "";

            //Liable
            for (BomRetunBo prod : mEmptyProducts) {
                if ((prod.getLiableQty() > 0)) {
                    for (AttributeListBO attr : mAttrList) {
                        mProductValue = "";
                        if (attr.getAttributeName().equalsIgnoreCase(EMPTY_PRODUCT_NAME)) {
                            mProductValue = "ED - " + (prod.getProductShortName() != null ? prod.getProductShortName() : prod.getProductName());
                        } else if (attr.getAttributeName().equalsIgnoreCase(EMPTY_PRODUCT_QTY)) {
                            mProductValue = formatValueInPrint(prod.getLiableQty(), attr.getmAttributePrecision());
                        } else if (attr.getAttributeName().equalsIgnoreCase(EMPTY_PRODUCT_PRICE)) {
                            mProductValue = formatValueInPrint(prod.getpSrp(), attr.getmAttributePrecision());
                        } else if (attr.getAttributeName().equalsIgnoreCase(EMPTY_PRODUCT_LINE_VALUE)) {
                            mProductValue = formatValueInPrint(prod.getLiableQty() * prod.getpSrp(), attr.getmAttributePrecision());
                        }

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
                        mProductValue = doAlign(mProductValue, ALIGNMENT_RIGHT, attr.getAttributeSpace());

                        sb.append(mProductValue);
                    }
                    sb.append("\n");
                }
            }

            for (BomRetunBo prod : mEmptyProducts) {
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
                        mProductValue = doAlign(mProductValue, ALIGNMENT_RIGHT, attr.getAttributeSpace());

                        sb.append(mProductValue);
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
                long splitvalue = Long.parseLong(splits[i]);
                if (i == 1 && splitvalue > 0) {
                    convertBuffer.append(" and ");
                }
                convertBuffer.append(numberToWord.convertNumberToWords(Long.parseLong(splits[i].toString())));
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
        total_line_value_incl_tax = 0;
        mBillLevelDiscountValue = 0;
        mEmptyTotalValue = 0;
        total_net_payable = 0;
        totalPriceOffValue = 0;
        isLogoEnabled = false;
        mSchemeValueByAmountType = 0;
        netSchemeAmount = 0;
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

                    int integerValue = (int) value;
                    int fractionValue = Integer.parseInt(fractionalStr);

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
}
