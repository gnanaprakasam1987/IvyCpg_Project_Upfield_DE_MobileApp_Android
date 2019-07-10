package com.ivy.cpg.view.order.tax;

import android.content.Context;
import android.util.SparseArray;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.cpg.view.order.tax.TaxBO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by mansoor on 18/1/18.
 */

public interface TaxInterface {

    void downloadProductTaxDetails();
    void downloadBillWiseTaxDetails();
    void insertInvoiceTaxList(String invoiceid, DBUtil db);
    void insertBillLevelTax(String orderId, DBUtil db);
    void loadTaxDetailsForPrint(String invoiceid);
    void loadTaxProductDetailsForPrint(String invoiceid);
    HashMap<String,Double> prepareProductTaxForPrint(Context context, String orderId, boolean isFromInvoice);
    void updateProductWiseExcludeTax();
    void saveProductLeveltax(String orderId, DBUtil db);
    void updateInvoiceIdInProductLevelTax(DBUtil db, String invid, String orderId);
    double applyBillWiseTax(double totalOrderValue);
    TaxBO cloneTaxBo(TaxBO taxBO);
    void calculateTaxOnTax(ProductMasterBO productMasterBO, TaxBO taxBO, boolean isFreeProduct);
    void insertProductLevelTaxForFreeProduct(String orderId, DBUtil db,String productId, TaxBO taxBO);
    HashMap<String, ArrayList<TaxBO>> getmTaxListByProductId();
    ArrayList<TaxBO> getGroupIdList();
    SparseArray<LinkedHashSet<TaxBO>> getTaxBoByGroupId();
    LinkedHashMap<String, HashSet<String>> getProductIdByTaxGroupId();
    double getTotalBillTaxAmount(boolean isOrder);
    LinkedHashMap<String, HashSet<String>> loadTaxFreeProductDetails(String invoiceid);
    HashMap<String, ArrayList<TaxBO>> getmTaxBoBatchProduct();
    ArrayList<TaxBO> getBillTaxList();
    LinkedHashMap<Integer, HashSet<Double>> getTaxPercentagerListByGroupId();
    double updateProductWiseIncludeTax(List<ProductMasterBO> productMasterBOS);
    float getTaxAmountByProduct(ProductMasterBO productMasterBO);


    //Project specific: Tax should be removed for scheme calculation.
    void removeTaxFromPrice(boolean isAllProducts);

    //Project specific: Removed tax is applied back to it after scheme calculation finished.
    void applyRemovedTax(LinkedList<ProductMasterBO> mOrderedProductList);

}
