package com.ivy.cpg.view.orderdelivery;

import android.content.Context;
import android.widget.Toast;

import com.ivy.cpg.view.order.OrderHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.CommonDialog;

import java.util.Vector;


public class OrderDeliveryPresenterImpl implements OrderDeliveryContractor.OrderDeliveryPresenter {
    private Vector<ProductMasterBO> productList = new Vector<>();
    private BusinessModel bmodel;
    private OrderDeliveryContractor.OrderDeliveryView orderDeliveryView;
    private Context context;
    private OrderHelper orderHelper;

    OrderDeliveryPresenterImpl(Context context,BusinessModel mBModel) {
        this.bmodel = mBModel;
        this.context = context;
        orderHelper = OrderHelper.getInstance(context);
    }

    @Override
    public void setView(OrderDeliveryContractor.OrderDeliveryView orderDeliveryView) {
        this.orderDeliveryView = orderDeliveryView;

    }

    @Override
    public void getProductData(String from) {

        orderHelper.downloadOrderedProducts();
        productList = orderHelper.getOrderedProductMasterBOS();

        if(from.equalsIgnoreCase("Edit")){
            orderDeliveryView.updateProductEditValues(productList);
        }else{
            orderDeliveryView.updateProductViewValues(productList);
        }

    }

    @Override
    public void getSchemeData() {
        orderDeliveryView.updateSchemeViewValues(orderHelper.getSchemeProductBOS());
    }

    @Override
    public void getAmountDetails() {
        orderDeliveryView.updateAmountDetails(orderHelper.getOrderDeliveryTotalValue(),
                orderHelper.getOrderDeliveryDiscountAmount(),orderHelper.getOrderDeliveryTaxAmount());
    }

    @Override
    public void saveOrderDeliveryDetail(final boolean isEdit, final String orderId) {
        if (orderHelper.isSIHAvailable(isEdit)) {

            final CommonDialog dialog = new CommonDialog(context.getApplicationContext(), context, "", context.getResources().getString(R.string.order_delivery_approve), false,
                    context.getResources().getString(R.string.ok),context.getResources().getString(R.string.cancel), new CommonDialog.positiveOnClickListener() {
                @Override
                public void onPositiveButtonClick() {

                    orderHelper.updateTableValues(context,orderId,isEdit);
                    Toast.makeText(
                            context,
                            "Approved",
                            Toast.LENGTH_SHORT).show();

                    orderDeliveryView.updateSaveStatus(true);

                }

            }, new CommonDialog.negativeOnClickListener(){
                @Override
                public void onNegativeButtonClick() {

                }
            });
            dialog.show();
            dialog.setCancelable(false);
        } else {
            Toast.makeText(
                    context,
                    context.getResources().getString(R.string.ordered_value_exceeds_sih_value_please_edit_the_order),
                    Toast.LENGTH_SHORT).show();

            orderDeliveryView.updateSaveStatus(false);
        }
    }

}
