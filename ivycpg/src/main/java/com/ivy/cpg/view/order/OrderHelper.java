package com.ivy.cpg.view.order;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.SchemeBO;
import com.ivy.sd.png.bo.SchemeProductBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

/**
 * Created by rajkumar on 30/1/18.
 */

public class OrderHelper {

    private static OrderHelper instance = null;
    private Context mContext;
    private BusinessModel businessModel;

    private Vector<ProductMasterBO> mSortedOrderedProducts;

    private OrderHelper(Context context) {
        this.mContext = context;
        this.businessModel = (BusinessModel) context;
    }


    public static OrderHelper getInstance(Context context) {
        if (instance == null) {
            instance = new OrderHelper(context);
        }
        return instance;
    }

    public Vector<ProductMasterBO> getSortedOrderedProducts() {
        return mSortedOrderedProducts;
    }

    public void setSortedOrderedProducts(Vector<ProductMasterBO> mSortedList) {
        this.mSortedOrderedProducts = mSortedList;
    }

    public boolean hasOrder(LinkedList<ProductMasterBO> orderedList) {

        int siz = orderedList.size();
        if (siz == 0)
            return false;
        for (int i = 0; i < siz; ++i) {
            ProductMasterBO product = orderedList.get(i);
            if (product.getOrderedCaseQty() > 0
                    || product.getOrderedPcsQty() > 0
                    || product.getOrderedOuterQty() > 0)
                return true;
        }
        return false;
    }


    //Method to check wether stock is available to deliver
    public boolean isStockAvailableToDeliver(List<ProductMasterBO> orderList) {
        try {

            HashMap<String, Integer> mDeliverQtyByProductId = new HashMap<>();

            for (ProductMasterBO product : orderList) {


                if (product.getOrderedPcsQty() > 0
                        || product.getOrderedCaseQty() > 0 || product
                        .getOrderedOuterQty() > 0) {

                    int totalQty = (product.getOrderedOuterQty() * product
                            .getOutersize())
                            + (product.getOrderedCaseQty() * product
                            .getCaseSize())
                            + (product.getOrderedPcsQty());
                    mDeliverQtyByProductId.put(product.getProductID(), totalQty);


                }
            }

            if (businessModel.configurationMasterHelper.IS_SCHEME_ON) {
                for (SchemeBO schemeBO : businessModel.schemeDetailsMasterHelper.getAppliedSchemeList()) {
                    if (schemeBO.getFreeProducts() != null) {
                        for (SchemeProductBO freeProductBO : schemeBO.getFreeProducts()) {
                            if (freeProductBO.getQuantitySelected() > 0) {

                                if (mDeliverQtyByProductId.get(freeProductBO.getProductId()) != null) {
                                    int qty = mDeliverQtyByProductId.get(freeProductBO.getProductId());
                                    mDeliverQtyByProductId.put(freeProductBO.getProductId(), (qty + freeProductBO.getQuantitySelected()));
                                } else {
                                    mDeliverQtyByProductId.put(freeProductBO.getProductId(), freeProductBO.getQuantitySelected());
                                }
                            }
                        }
                    }
                }


            }

            for (String productId : mDeliverQtyByProductId.keySet()) {
                ProductMasterBO product = businessModel.productHelper.getProductMasterBOById(productId);
                if (product != null) {
                    if (mDeliverQtyByProductId.get(productId) > product.getSIH())
                        return false;
                }
            }
        } catch (Exception ex) {
            Commons.printException(ex);
            return false;
        }
        return true;
    }

    public boolean isAllScanned() {

        for (ProductMasterBO productBO : businessModel.productHelper.getProductMaster()) {
            int totalQty = productBO.getOrderedPcsQty() + (productBO.getOrderedCaseQty() * productBO.getCaseSize())
                    + (productBO.getOrderedOuterQty() * productBO.getOutersize());
            if (totalQty > 0 && productBO.getScannedProduct() == 1) {
                if (totalQty != productBO.getTotalScannedQty()) {
                    return false;
                }
            }


        }
        return true;


    }


    // To check whether reason provided for un satisfied indicative order
    public boolean isReasonProvided(LinkedList<ProductMasterBO> orderedList) {
        int siz = orderedList.size();
        if (siz == 0)
            return false;
        for (int i = 0; i < siz; ++i) {
            ProductMasterBO product =orderedList.get(i);
            if (businessModel.configurationMasterHelper.IS_SHOW_ORDER_REASON) {
                if (product.getOrderedCaseQty() > 0 || product.getOrderedPcsQty() > 0 || product.getOrderedOuterQty() > 0) {
                    if (product.getSoreasonId() == 0)
                        return false;
                }
            } else {
                if (product.getOrderedCaseQty() > 0)
                    if (product.getOrderedCaseQty() < product.getIndicativeOrder_oc())
                        if (product.getSoreasonId() == 0)
                            return false;
            }
        }
        return true;
    }


    /**
     * @AUTHOR Rajesh.K
     * <p>
     * Method used to add Off invoice scheme  free product in Last ordered  product (schemeproduct object).So that
     * we can show in Print
     */
    public void updateOffInvoiceSchemeInProductOBJ(LinkedList<ProductMasterBO> mOrderedProductList) {
        ProductMasterBO productBO = mOrderedProductList.get(mOrderedProductList.size() - 1);
        if (productBO != null) {
            ArrayList<SchemeBO> offInvoiceSchemeList = businessModel.schemeDetailsMasterHelper.getmOffInvoiceAppliedSchemeList();
            if (offInvoiceSchemeList != null) {
                for (SchemeBO schemeBO : offInvoiceSchemeList) {
                    if (schemeBO.isQuantityTypeSelected()) {
                        updateSchemeFreeproduct(schemeBO, productBO);
                    }
                }
            }
        }

    }

    /**
     * Method to add free product list into any one of scheme buy product
     *
     * @param schemeBO
     * @param productBO
     */
    public void updateSchemeFreeproduct(SchemeBO schemeBO,
                                        ProductMasterBO productBO) {
        List<SchemeProductBO> freeProductList = schemeBO.getFreeProducts();
        if (productBO.getSchemeProducts() == null) {
            productBO.setSchemeProducts(new ArrayList<SchemeProductBO>());
        }

        if (freeProductList != null) {
            for (SchemeProductBO freeProductBo : freeProductList) {
                if (freeProductBo.getQuantitySelected() > 0) {
                    ProductMasterBO product = businessModel.productHelper
                            .getProductMasterBOById(freeProductBo
                                    .getProductId());
                    if (product != null) {
                        productBO.getSchemeProducts().add(freeProductBo);
                    }
                }
            }
        }

    }

    public boolean isTaxAvailableForAllOrderedProduct(LinkedList<ProductMasterBO> mOrderedProductList) {
        for (ProductMasterBO bo : mOrderedProductList) {
            if (businessModel.productHelper.taxHelper.getmTaxListByProductId() == null) {
                return false;
            }
            if (businessModel.productHelper.taxHelper.getmTaxListByProductId().get(bo.getProductID()) == null
                    || businessModel.productHelper.taxHelper.getmTaxListByProductId().get(bo.getProductID()).size() == 0) {
                return false;
            }
        }
        return true;
    }


    public boolean isTaxAppliedForAnyProduct(LinkedList<ProductMasterBO> mOrderedProductList) {

        int productsCount = mOrderedProductList.size();

        for (int i = 0; i < productsCount; i++) {
            ProductMasterBO productBO = mOrderedProductList.get(i);

            if (productBO.getOrderedCaseQty() > 0
                    || productBO.getOrderedPcsQty() > 0
                    || productBO.getOrderedOuterQty() > 0) {

                if (productBO.getTaxValue() > 0)
                    return true;
            }
        }

        return false;
    }



    //////////////////////////// Print ////////
    int print_count;
    public int getPrint_count() {
        return print_count;
    }
    public int getPrintCount(Context mContext) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db.selectSQL("select print_count from InvoiceMaster where invoiceNo='" + businessModel.invoiceNumber + "'");
            if (c != null) {
                if (c.moveToNext()) {

                    Commons.print("print_count," + c.getInt(0) + "");
                    print_count = c.getInt(0);
                    c.close();
                    db.closeDB();
                }
            }
            c.close();
            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return print_count;
    }




}
