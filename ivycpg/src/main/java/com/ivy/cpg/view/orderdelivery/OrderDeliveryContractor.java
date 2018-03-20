package com.ivy.cpg.view.orderdelivery;

import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.SchemeProductBO;

import java.util.ArrayList;
import java.util.Vector;


public interface OrderDeliveryContractor {

    interface OrderDeliveryPresenter{
        void setView(OrderDeliveryView orderDeliveryView);
        void getProductData(String from);
        void getSchemeData();
        void getAmountDetails();
        void saveOrderDeliveryDetail(boolean isEdit,String orderId);
    }

    interface OrderDeliveryView{
        void updateProductViewValues(Vector<ProductMasterBO> productList);
        void updateProductEditValues(Vector<ProductMasterBO> productList);
        void updateSchemeViewValues(ArrayList<SchemeProductBO> schemeProductBOS);
        void updateAmountDetails(String orderVal,String discountAmt,String taxAmt);
        void updateSaveStatus(boolean isSuccess);
    }

}
