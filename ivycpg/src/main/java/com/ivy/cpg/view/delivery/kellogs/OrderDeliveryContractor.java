package com.ivy.cpg.view.delivery.kellogs;

import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.cpg.view.order.scheme.SchemeProductBO;

import java.util.ArrayList;
import java.util.Vector;


public interface OrderDeliveryContractor {

    interface OrderDeliveryPresenter{
        void setView(OrderDeliveryView orderDeliveryView);
        void getProductData(String from);
        void getSchemeData();
        void getAmountDetails(boolean isEdit);
        void saveOrderDeliveryDetail(boolean isEdit,String orderId,String menuCode,double totalOrderValue,double totalReturnValue,String referenceId);
        void doPrintActivity(String orderId);
        int getRemainingStock(String productId);
    }

    interface OrderDeliveryView{
        void updateProductViewValues(Vector<ProductMasterBO> productList);
        void updateProductEditValues(Vector<ProductMasterBO> productList);
        void updateSchemeViewValues(ArrayList<SchemeProductBO> schemeProductBOS);
        void updateAmountDetails(String orderVal,String discountAmt,String taxAmt,String totalOrderAmt);
        void updateSaveStatus(boolean isSuccess);
        void updatePrintStatus(String msg,boolean status);
    }

}
