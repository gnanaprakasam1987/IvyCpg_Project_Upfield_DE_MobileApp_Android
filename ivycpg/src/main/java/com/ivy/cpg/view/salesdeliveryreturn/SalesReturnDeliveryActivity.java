package com.ivy.cpg.view.salesdeliveryreturn;


import com.ivy.core.base.view.BaseActivity;
import com.ivy.sd.png.asean.view.R;

public class SalesReturnDeliveryActivity extends BaseActivity {
    @Override
    public int getLayoutId() {
        return R.layout.activity_sales_returndelivery;
    }

    @Override
    protected void initVariables() {

    }

    @Override
    public void initializeDi() {

    }

    @Override
    protected void getMessageFromAliens() {

    }

    @Override
    protected void setUpViews() {
        SalesReturnDeliveryFragment salesReturnDeliveryFragment = new SalesReturnDeliveryFragment();
        addFragment(R.id.container_salesReturn,salesReturnDeliveryFragment,false,false,0);
    }
}
