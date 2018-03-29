package com.ivy.cpg.view.orderdelivery;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.StandardListMasterConstants;

import java.util.Vector;


public class OrderDeliveryPresenterImpl implements OrderDeliveryContractor.OrderDeliveryPresenter {
    private BusinessModel bmodel;
    private OrderDeliveryContractor.OrderDeliveryView orderDeliveryView;
    private Context context;
    private OrderDeliveryHelper orderDeliveryHelper;

    OrderDeliveryPresenterImpl(Context context,BusinessModel mBModel) {
        this.bmodel = mBModel;
        this.context = context;
        orderDeliveryHelper = OrderDeliveryHelper.getInstance(context);
    }

    @Override
    public void setView(OrderDeliveryContractor.OrderDeliveryView orderDeliveryView) {
        this.orderDeliveryView = orderDeliveryView;

    }

    @Override
    public void getProductData(String from) {

        Vector<ProductMasterBO> productList = orderDeliveryHelper.getOrderedProductMasterBOS();

        if(from.equalsIgnoreCase("Edit")){
            orderDeliveryView.updateProductEditValues(productList);
        }else{
            orderDeliveryView.updateProductViewValues(productList);
        }

    }

    @Override
    public void getSchemeData() {
        orderDeliveryView.updateSchemeViewValues(orderDeliveryHelper.getSchemeProductBOS());
    }

    @Override
    public void getAmountDetails(boolean isEdit) {
        orderDeliveryHelper.getProductTotalValue();

        double discountVal = SDUtil.convertToDouble(orderDeliveryHelper.getOrderDeliveryDiscountAmount());
        double orderValue = SDUtil.convertToDouble(orderDeliveryHelper.getOrderDeliveryTotalValue());
        double totalTaxVal = SDUtil.convertToDouble(orderDeliveryHelper.getOrderDeliveryTaxAmount());
        double orderTaxIncludeVal = SDUtil.convertToDouble(orderDeliveryHelper.getOrderDeliveryTaxAmount()) +
                                    orderValue - (isEdit?0.0:discountVal);

        orderDeliveryView.updateAmountDetails(String.valueOf(bmodel.formatValueBasedOnConfig(orderValue)),
                                                isEdit?"0.0":String.valueOf(bmodel.formatValueBasedOnConfig(discountVal)),
                                                String.valueOf(bmodel.formatValueBasedOnConfig(totalTaxVal)),
                                                String.valueOf(bmodel.formatValueBasedOnConfig(orderTaxIncludeVal)));
    }

    @Override
    public void saveOrderDeliveryDetail(final boolean isEdit, final String orderId) {
        if(orderDeliveryHelper.getTotalProductQty() == 0)
            Toast.makeText(
                    context,
                    context.getResources().getString(R.string.no_ordered_products_found),
                    Toast.LENGTH_SHORT).show();
        else if (orderDeliveryHelper.isSIHAvailable(isEdit)) {

            final CommonDialog dialog = new CommonDialog(context.getApplicationContext(), context, "", context.getResources().getString(R.string.order_delivery_approve), false,
                    context.getResources().getString(R.string.ok),context.getResources().getString(R.string.cancel), new CommonDialog.positiveOnClickListener() {
                @Override
                public void onPositiveButtonClick() {
                    new UpdateOrderDeliveryTable(orderId,context,isEdit).execute();
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

    @Override
    public void goToPrintActivity() {

    }

    public class UpdateOrderDeliveryTable extends AsyncTask<Void,Void,Boolean> {

        private String orderId;
        private Context context;
        private boolean isEdit;

        private UpdateOrderDeliveryTable(String orderId, Context context,boolean isEdit){
            this.orderId = orderId;
            this.context = context;
            this.isEdit = isEdit;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            return orderDeliveryHelper.updateTableValues(context, orderId,isEdit);
        }

        @Override
        protected void onPostExecute(Boolean isSuccess) {
            if (isSuccess) {
                Toast.makeText(
                        context,
                        context.getResources().getString(R.string.invoice_generated),
                        Toast.LENGTH_SHORT).show();

                bmodel.mCommonPrintHelper.xmlRead(".xml", false,orderDeliveryHelper.getOrderedProductMasterBOS() , null);

                bmodel.writeToFile(String.valueOf(bmodel.mCommonPrintHelper.getInvoiceData()),
                        StandardListMasterConstants.PRINT_FILE_INVOICE + bmodel.invoiceNumber, "/" + DataMembers.PRINT_FILE_PATH);

            }
            else
                Toast.makeText(
                        context,
                        context.getResources().getString(R.string.not_able_to_generate_invoice),
                        Toast.LENGTH_SHORT).show();

            orderDeliveryView.updateSaveStatus(isSuccess);
        }
    }

}
