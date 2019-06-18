package com.ivy.cpg.view.delivery.kellogs;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.StandardListMasterConstants;

import java.io.OutputStream;
import java.util.UUID;
import java.util.Vector;


public class OrderDeliveryPresenterImpl implements OrderDeliveryContractor.OrderDeliveryPresenter {
    private BusinessModel bmodel;
    private OrderDeliveryContractor.OrderDeliveryView orderDeliveryView;
    private Context context;
    private OrderDeliveryHelper orderDeliveryHelper;

    private int mPrintCountInput = 1;
    private int mDataPrintCount = 0;

    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    OrderDeliveryPresenterImpl(Context context, BusinessModel mBModel) {
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

        if (from.equalsIgnoreCase("Edit")) {
            orderDeliveryView.updateProductEditValues(productList);
        } else {
            orderDeliveryView.updateProductViewValues(productList);
        }

    }

    @Override
    public int getRemainingStock(String productId) {

        int remainingSIH=bmodel.productHelper.getProductMasterBOById(productId).getDSIH();
        Vector<ProductMasterBO> productList = orderDeliveryHelper.getOrderedProductMasterBOS();
        for(ProductMasterBO productMasterBO:productList){
            if(productId.equals(productMasterBO.getProductID())){

                int totalQty = (productMasterBO.getOrderedCaseQty() * productMasterBO.getCaseSize())
                        + (productMasterBO.getOrderedPcsQty())
                        + (productMasterBO.getOrderedOuterQty() * productMasterBO.getOutersize())
                        + productMasterBO.getRepPieceQty()
                        + (productMasterBO.getRepCaseQty()*productMasterBO.getCaseSize())
                        + (productMasterBO.getRepOuterQty()*productMasterBO.getOutersize());

                remainingSIH=productMasterBO.getDSIH()-totalQty;

            }
        }

        if(remainingSIH>0)
        return remainingSIH;
        else return 0;
    }

    @Override
    public void getSchemeData() {
        orderDeliveryView.updateSchemeViewValues(orderDeliveryHelper.getSchemeProductBOS());
    }

    @Override
    public void getAmountDetails(boolean isEdit) {
        orderDeliveryHelper.getProductTotalValue(isEdit);

        double discountVal = SDUtil.convertToDouble(orderDeliveryHelper.getOrderDeliveryDiscountAmount());
        double orderValue = SDUtil.convertToDouble(orderDeliveryHelper.getOrderDeliveryTotalValue());
        double totalTaxVal = SDUtil.convertToDouble(orderDeliveryHelper.getOrderDeliveryTaxAmount());
        double orderTaxIncludeVal = (SDUtil.convertToDouble(orderDeliveryHelper.getOrderDeliveryTaxAmount()) +
                orderValue) - (isEdit ? 0.0 : discountVal);

        orderDeliveryView.updateAmountDetails(String.valueOf(bmodel.formatBasedOnCurrency(orderValue)),
                isEdit ? "0.0" : String.valueOf(bmodel.formatBasedOnCurrency(discountVal)),
                String.valueOf(bmodel.formatBasedOnCurrency(totalTaxVal)),
                String.valueOf(bmodel.formatBasedOnCurrency(orderTaxIncludeVal)));
    }

    @Override
    public void saveOrderDeliveryDetail(final boolean isEdit, final String orderId,final String menuCode,double totalOrderValue,double totalReturnValue,final String referenceId) {
        if (orderDeliveryHelper.getTotalProductQty() == 0) {
            Toast.makeText(
                    context,
                    context.getResources().getString(R.string.no_ordered_products_found),
                    Toast.LENGTH_SHORT).show();
        } else if (orderDeliveryHelper.isSIHAvailable(isEdit)) {

            //Removed as per JIRA Changes
//            if(bmodel.configurationMasterHelper.SHOW_SALES_RETURN_IN_ORDER
//                    && bmodel.retailerMasterBO.getRpTypeCode() != null
//                    && "CASH".equals(bmodel.retailerMasterBO.getRpTypeCode()) && totalOrderValue<totalReturnValue){
//                Toast.makeText(context
//                        ,context.getResources().getString(R.string.sales_return_value_exceeds_order_value),Toast.LENGTH_LONG).show();
//                return;
//            }
            if(totalOrderValue<totalReturnValue){
                Toast.makeText(context
                        ,context.getResources().getString(R.string.sales_return_value_exceeds_order_value),Toast.LENGTH_LONG).show();
                return;
            }

            final CommonDialog dialog = new CommonDialog(context.getApplicationContext(), context, "", context.getResources().getString(R.string.order_delivery_approve), false,
                    context.getResources().getString(R.string.ok), context.getResources().getString(R.string.cancel), new CommonDialog.PositiveClickListener() {
                @Override
                public void onPositiveButtonClick() {
                    new UpdateOrderDeliveryTable(orderId, context, isEdit,menuCode,referenceId).execute();
                }

            }, new CommonDialog.negativeOnClickListener() {
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
    public void doPrintActivity(String orderId) {
        bmodel.mCommonPrintHelper.xmlRead("invoice", false, orderDeliveryHelper.preparePrintData(context, orderId), null, null,null,null);

        bmodel.writeToFile(String.valueOf(bmodel.mCommonPrintHelper.getInvoiceData()),
                StandardListMasterConstants.PRINT_FILE_INVOICE + bmodel.invoiceNumber, "/" + DataMembers.PRINT_FILE_PATH, "");
        orderDeliveryView.updateSaveStatus(true);
    }

    public class UpdateOrderDeliveryTable extends AsyncTask<Void, Void, Boolean> {

        private String orderId,menuCode;
        private Context context;
        private boolean isEdit;
        private String referenceId;

        private UpdateOrderDeliveryTable(String orderId, Context context, boolean isEdit,String menuCode,String referenceId) {
            this.orderId = orderId;
            this.context = context;
            this.isEdit = isEdit;
            this.menuCode=menuCode;
            this.referenceId = referenceId;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if(!isEdit)
                orderDeliveryHelper.updateDiscountInLineValue(context,orderId);

            return orderDeliveryHelper.updateTableValues(context, orderId, isEdit,menuCode,referenceId);
        }

        @Override
        protected void onPostExecute(Boolean isSuccess) {
            if (isSuccess) {
                Toast.makeText(
                        context,
                        context.getResources().getString(R.string.invoice_generated),
                        Toast.LENGTH_SHORT).show();

                if (bmodel.configurationMasterHelper.COMMON_PRINT_ZEBRA
                        || bmodel.configurationMasterHelper.COMMON_PRINT_BIXOLON || bmodel.configurationMasterHelper.COMMON_PRINT_SCRYBE || bmodel.configurationMasterHelper.COMMON_PRINT_LOGON
                        || bmodel.configurationMasterHelper.COMMON_PRINT_INTERMEC) {

                    if (!isEdit)
                        orderDeliveryHelper.getOrderedProductMasterBOS().get(orderDeliveryHelper.getOrderedProductMasterBOS().size() - 1).
                                setSchemeProducts(orderDeliveryHelper.downloadSchemeFreePrint(context, orderId));

                    orderDeliveryHelper.updateTaxInLineValue(context,orderId);

                    bmodel.mCommonPrintHelper.xmlRead("invoice", false, orderDeliveryHelper.getOrderedProductMasterBOS(), null, null, null, null);

                    bmodel.writeToFile(String.valueOf(bmodel.mCommonPrintHelper.getInvoiceData()),
                            StandardListMasterConstants.PRINT_FILE_INVOICE + bmodel.invoiceNumber, "/" + DataMembers.PRINT_FILE_PATH, "");
                    orderDeliveryView.updateSaveStatus(isSuccess);
                }

            } else
                Toast.makeText(
                        context,
                        context.getResources().getString(R.string.not_able_to_generate_invoice),
                        Toast.LENGTH_SHORT).show();
        }
    }

    class Print extends AsyncTask<String, Void, Boolean> {

        String orderId;

        Print(String orderId) {
            this.orderId = orderId;
        }

        protected void onPreExecute() {
            orderDeliveryView.updatePrintStatus("Connecting...", true);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            bmodel.mCommonPrintHelper.xmlRead("invoice", false, orderDeliveryHelper.preparePrintData(context, orderId), null, null,null,null);

            bmodel.writeToFile(String.valueOf(bmodel.mCommonPrintHelper.getInvoiceData()),
                    StandardListMasterConstants.PRINT_FILE_INVOICE + bmodel.invoiceNumber, "/" + DataMembers.PRINT_FILE_PATH, "");
            doInterMecPrint(getMacAddressFieldText());
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            boolean isPrintSuccess = false;
            if (mPrintCountInput == mDataPrintCount)
                isPrintSuccess = true;


            String msg;
            if (isPrintSuccess) {
                msg = context.getResources().getString(
                        R.string.printed_successfully);
                orderDeliveryView.updatePrintStatus(msg, false);
            } else {
                msg = "Error";
                orderDeliveryView.updatePrintStatus(msg, false);
            }
        }

    }

    private void doInterMecPrint(String macAddress) {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice mBluetoothDevice;
        BluetoothSocket mBluetoothSocket;
        OutputStream mOutputStream = null;
        try {
            if (macAddress.equals(""))
                orderDeliveryView.updatePrintStatus("Mac address is empty...", true);

            mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(macAddress);
            mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(SPP_UUID);
            mBluetoothSocket.connect();
            orderDeliveryView.updatePrintStatus("Printing...", true);

            for (int i = 0; i < mPrintCountInput; i++) {
                mOutputStream = mBluetoothSocket.getOutputStream();
                mOutputStream.write((bmodel.mCommonPrintHelper.getInvoiceData().toString()).getBytes());
                mOutputStream.flush();
                mDataPrintCount++;
            }

            mOutputStream.close();
            mBluetoothSocket.close();
        } catch (Exception e) {
            Commons.printException(e);
            orderDeliveryView.updatePrintStatus("Connection Failed", false);
        }
    }

    public String getMacAddressFieldText() {
        String macAddress = null;
        try {
            // String macAddress = "00:22:58:3A:CD:46";
            SharedPreferences pref = context.getSharedPreferences("PRINT",
                    Context.MODE_PRIVATE);
            macAddress = pref.getString("MAC", "");
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("MAC", macAddress);
            editor.apply();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return macAddress;
    }

}
