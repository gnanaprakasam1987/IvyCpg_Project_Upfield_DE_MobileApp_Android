package com.ivy.cpg.view.delivery.kellogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.cpg.view.salesreturn.SalesReturnReasonBO;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.cpg.view.order.scheme.SchemeProductBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.view.OnSingleClickListener;
import com.ivy.sd.print.CommonPrintPreviewActivity;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FontUtils;

import java.util.ArrayList;
import java.util.Vector;

public class OrderDeliveryDetailActivity extends IvyBaseActivityNoActionBar implements OrderDeliveryContractor.OrderDeliveryView {

    private BusinessModel bmodel;
    private LinearLayout llContent;
    OrderDeliveryPresenterImpl orderDeliveryPresenter;
    private RecyclerView recyclerView;
    private InputMethodManager inputManager;
    private EditText QUANTITY;
    private String append = "";
    private int invoiceStatus = 0;
    private boolean isEdit;
    private TextView discount_value, taxValue, orderValue, orderBaseValue;
    final String Str_VIEW = "VIEW";
    final String Str_EDIT = "EDIT";
    private boolean isPrintClicked;
    private String orderId;
    private String referenceId = "";
    private double totalReturnValue,totalOrderValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_delivery_detail);

        inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

        Toolbar toolbar = findViewById(R.id.toolbar);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle("Order Delivery Detail");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }


        invoiceStatus = getIntent().getExtras().getInt("InvoiceStatus");

        initializeViews();

        orderDeliveryPresenter = new OrderDeliveryPresenterImpl(this,bmodel);
        orderDeliveryPresenter.setView(this);

        orderDeliveryPresenter.getProductData(getIntent().getExtras().getString("From"));

        if(getIntent().getExtras().getString("From").equals(Str_VIEW)){
            orderDeliveryPresenter.getSchemeData();
        }

        if(getIntent().getExtras().getString("From").equals(Str_EDIT))
            isEdit = true;
        else
            isEdit = false;

        orderDeliveryPresenter.getAmountDetails(isEdit);
    }

    private void initializeViews(){

        recyclerView = findViewById(R.id.order_detail_product_list);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        llContent = findViewById(R.id.ll_content);

        ((TextView)findViewById(R.id.discount_value_title)).setTypeface(FontUtils.getFontRoboto(OrderDeliveryDetailActivity.this, FontUtils.FontType.LIGHT));
        ((TextView)findViewById(R.id.tax_value_title)).setTypeface(FontUtils.getFontRoboto(OrderDeliveryDetailActivity.this, FontUtils.FontType.LIGHT));
        ((TextView)findViewById(R.id.order_value_title)).setTypeface(FontUtils.getFontRoboto(OrderDeliveryDetailActivity.this, FontUtils.FontType.LIGHT));
        ((TextView)findViewById(R.id.ord_value_title)).setTypeface(FontUtils.getFontRoboto(OrderDeliveryDetailActivity.this, FontUtils.FontType.LIGHT));

        discount_value = findViewById(R.id.discount_value);
        taxValue = findViewById(R.id.tax_value);
        orderValue = findViewById(R.id.order_value);
        orderBaseValue = findViewById(R.id.ord_value);

        discount_value.setTypeface(FontUtils.getFontRoboto(OrderDeliveryDetailActivity.this, FontUtils.FontType.MEDIUM));
        taxValue.setTypeface(FontUtils.getFontRoboto(OrderDeliveryDetailActivity.this, FontUtils.FontType.MEDIUM));
        orderValue.setTypeface(FontUtils.getFontRoboto(OrderDeliveryDetailActivity.this, FontUtils.FontType.MEDIUM));
        orderBaseValue.setTypeface(FontUtils.getFontRoboto(OrderDeliveryDetailActivity.this, FontUtils.FontType.MEDIUM));

        discount_value.setTextColor(Color.parseColor("#000000"));
        taxValue.setTextColor(Color.parseColor("#000000"));
        orderValue.setTextColor(Color.parseColor("#000000"));
        orderBaseValue.setTextColor(Color.parseColor("#000000"));

        if(isEdit) {
            discount_value.setVisibility(View.GONE);
            (findViewById(R.id.discount_value_title)).setVisibility(View.GONE);
        }

        if(invoiceStatus == 1) {
            findViewById(R.id.accept_btn).setVisibility(View.GONE);
            findViewById(R.id.footer).setVisibility(View.GONE);
        }

        orderId = getIntent().getExtras().getString("OrderId");
        referenceId = getIntent().getExtras().getString("RefId");

        findViewById(R.id.accept_btn).setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                orderDeliveryPresenter.saveOrderDeliveryDetail(
                        isEdit,orderId,getIntent().getExtras().getString("menuCode"),totalOrderValue,totalReturnValue,referenceId);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        //session out if user id becomes 0
        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this,
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void updateProductViewValues(Vector<ProductMasterBO> productList) {
        findViewById(R.id.card_view).setVisibility(View.GONE);

        llContent.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(this);

        if(productList.size() > 0){

            View cardView = inflater.inflate(R.layout.order_delivery_base_card_view, null);
            LinearLayout card_view_items = cardView.findViewById(R.id.card_view_items);

            View headerView = inflater.inflate(R.layout.order_delivery_detail_item, null);
            setProductView(headerView,true,FontUtils.FontType.LIGHT,null);
            headerView.setBackgroundColor(Color.parseColor("#000000"));

            ViewGroup.LayoutParams headerParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.list_header_height));
            headerView.setLayoutParams(headerParams);

            card_view_items.addView(headerView);

            View childViews;
            for(ProductMasterBO productMasterBO : productList){
                childViews = inflater.inflate(R.layout.order_delivery_detail_item, null);
                setProductView(childViews,false,FontUtils.FontType.MEDIUM,productMasterBO);

                ViewGroup.LayoutParams childParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.dimens_50dp));
                childViews.setLayoutParams(childParams);

                card_view_items.addView(childViews);
            }

            llContent.addView(cardView);


        }


    }

    private void setProductView(View view,boolean isHeader, FontUtils.FontType fontType,ProductMasterBO productMasterBO){

        if(isHeader) {
            ((TextView)view.findViewById(R.id.prod_name)).setTypeface(FontUtils.getFontRoboto(OrderDeliveryDetailActivity.this, fontType));
            ((TextView) view.findViewById(R.id.prod_name)).setText(getResources().getString(R.string.product_name));
            ((TextView) view.findViewById(R.id.prod_name)).setTextColor(Color.parseColor("#FFFFFF"));
        }
        else {
            ((TextView)view.findViewById(R.id.prod_name)).setTypeface(FontUtils.getFontRoboto(OrderDeliveryDetailActivity.this, fontType));
            ((TextView) view.findViewById(R.id.prod_name)).setText(productMasterBO.getProductName());
            ((TextView) view.findViewById(R.id.prod_name)).setTextColor(Color.parseColor("#000000"));
        }


        if (!bmodel.configurationMasterHelper.SHOW_DELIVERY_PC)
            (view.findViewById(R.id.piece_qty)).setVisibility(View.GONE);
        else if(isHeader) {
            try {
                ((TextView) view.findViewById(R.id.piece_qty)).setText(getResources().getString(R.string.piece));
                ((TextView) view.findViewById(R.id.piece_qty)).setTextColor(Color.parseColor("#FFFFFF"));
                ((TextView) view.findViewById(R.id.piece_qty)).setTypeface(FontUtils.getFontRoboto(OrderDeliveryDetailActivity.this, fontType));
                if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                        R.id.piece_qty).getTag()) != null)
                    ((TextView) view.findViewById(R.id.piece_qty))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(view.findViewById(
                                            R.id.piece_qty).getTag()));
            } catch (Exception e) {
                Commons.printException(e + "");
            }
        }
        else{
            ((TextView) view.findViewById(R.id.piece_qty)).setText(String.valueOf(productMasterBO.getOrderedPcsQty()));
            ((TextView) view.findViewById(R.id.piece_qty)).setTypeface(FontUtils.getFontRoboto(OrderDeliveryDetailActivity.this, FontUtils.FontType.LIGHT));
            ((TextView) view.findViewById(R.id.piece_qty)).setTextColor(Color.parseColor("#000000"));
        }

        if (!bmodel.configurationMasterHelper.SHOW_DELIVERY_CA)
            ( view.findViewById(R.id.case_qty)).setVisibility(View.GONE);
        else if(isHeader){
            try {
                ((TextView) view.findViewById(R.id.case_qty)).setText(getResources().getString(R.string.case_u));
                ((TextView) view.findViewById(R.id.case_qty)).setTypeface(FontUtils.getFontRoboto(OrderDeliveryDetailActivity.this, fontType));
                ((TextView) view.findViewById(R.id.case_qty)).setTextColor(Color.parseColor("#FFFFFF"));
                if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                        R.id.case_qty).getTag()) != null)
                    ((TextView) view.findViewById(R.id.case_qty))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(view.findViewById(
                                            R.id.case_qty).getTag()));
            } catch (Exception e) {
                Commons.printException(e + "");
            }
        }
        else {
            ((TextView) view.findViewById(R.id.case_qty)).setText(String.valueOf(productMasterBO.getOrderedCaseQty()));
            ((TextView) view.findViewById(R.id.case_qty)).setTypeface(FontUtils.getFontRoboto(OrderDeliveryDetailActivity.this, FontUtils.FontType.LIGHT));
            ((TextView) view.findViewById(R.id.case_qty)).setTextColor(Color.parseColor("#000000"));
        }

        if (!bmodel.configurationMasterHelper.SHOW_DELIVERY_OU)
            (view.findViewById(R.id.outer_qty)).setVisibility(View.GONE);
        else if (isHeader){
            try {
                ((TextView) view.findViewById(R.id.outer_qty)).setText(getResources().getString(R.string.outer_label));
                ((TextView) view.findViewById(R.id.outer_qty)).setTypeface(FontUtils.getFontRoboto(OrderDeliveryDetailActivity.this, fontType));
                ((TextView) view.findViewById(R.id.outer_qty)).setTextColor(Color.parseColor("#FFFFFF"));
                if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                        R.id.outer_qty).getTag()) != null)
                    ((TextView) view.findViewById(R.id.outer_qty))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(view.findViewById(
                                            R.id.outer_qty).getTag()));
            } catch (Exception e) {
                Commons.printException(e + "");
            }
        }
        else {
            ((TextView) view.findViewById(R.id.outer_qty)).setText(String.valueOf(productMasterBO.getOrderedOuterQty()));
            ((TextView) view.findViewById(R.id.outer_qty)).setTypeface(FontUtils.getFontRoboto(OrderDeliveryDetailActivity.this, FontUtils.FontType.LIGHT));
            ((TextView) view.findViewById(R.id.outer_qty)).setTextColor(Color.parseColor("#000000"));
        }

        if (!bmodel.configurationMasterHelper.IS_STOCK_IN_HAND)
            (view.findViewById(R.id.sih_qty)).setVisibility(View.GONE);
        else if (isHeader){
            try {
                ((TextView) view.findViewById(R.id.sih_qty)).setText(getResources().getString(R.string.sih));
                ((TextView) view.findViewById(R.id.sih_qty)).setTypeface(FontUtils.getFontRoboto(OrderDeliveryDetailActivity.this, fontType));
                ((TextView) view.findViewById(R.id.sih_qty)).setTextColor(Color.parseColor("#FFFFFF"));
                if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                        R.id.sih_qty).getTag()) != null)
                    ((TextView) view.findViewById(R.id.sih_qty))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(view.findViewById(
                                            R.id.sih_qty).getTag()));
            } catch (Exception e) {
                Commons.printException(e + "");
            }
        }
        else {
            ((TextView) view.findViewById(R.id.sih_qty)).setText(String.valueOf(productMasterBO.getDSIH()));
            ((TextView) view.findViewById(R.id.sih_qty)).setTypeface(FontUtils.getFontRoboto(OrderDeliveryDetailActivity.this, FontUtils.FontType.LIGHT));
            ((TextView) view.findViewById(R.id.sih_qty)).setTextColor(Color.parseColor("#000000"));
        }

        if (!bmodel.configurationMasterHelper.SHOW_SALES_RETURN_IN_DELIVERY)
            (view.findViewById(R.id.sales_return_qty)).setVisibility(View.GONE);
        else if (isHeader){
            try {
                ((TextView) view.findViewById(R.id.sales_return_qty)).setText(getResources().getString(R.string.sr));
                ((TextView) view.findViewById(R.id.sales_return_qty)).setTypeface(FontUtils.getFontRoboto(OrderDeliveryDetailActivity.this, fontType));
                ((TextView) view.findViewById(R.id.sales_return_qty)).setTextColor(Color.parseColor("#FFFFFF"));
                if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                        R.id.sales_return_qty).getTag()) != null)
                    ((TextView) view.findViewById(R.id.sales_return_qty))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(view.findViewById(
                                            R.id.sales_return_qty).getTag()));
            } catch (Exception e) {
                Commons.printException(e + "");
            }
        }
        else {
            int total = 0;
            if (productMasterBO.getSalesReturnReasonList() != null) {
                for (SalesReturnReasonBO obj : productMasterBO.getSalesReturnReasonList()) {
                    total = total + obj.getPieceQty() + (obj.getCaseQty() * obj.getCaseSize()) + (obj.getOuterQty() * obj.getOuterSize());
                    totalReturnValue = totalReturnValue + (total * productMasterBO.getSrp());
                }
            }
            ((TextView) view.findViewById(R.id.sales_return_qty)).setText(String.valueOf(total));
            ((TextView) view.findViewById(R.id.sales_return_qty)).setTypeface(FontUtils.getFontRoboto(OrderDeliveryDetailActivity.this, FontUtils.FontType.LIGHT));
            ((TextView) view.findViewById(R.id.sales_return_qty)).setTextColor(Color.parseColor("#000000"));
        }

        if (!bmodel.configurationMasterHelper.SHOW_SALES_RETURN_IN_DELIVERY)
            (view.findViewById(R.id.sales_replace_qty)).setVisibility(View.GONE);
        else if (isHeader){
            try {
                ((TextView) view.findViewById(R.id.sales_replace_qty)).setText(getResources().getString(R.string.replacement));
                ((TextView) view.findViewById(R.id.sales_replace_qty)).setTypeface(FontUtils.getFontRoboto(OrderDeliveryDetailActivity.this, fontType));
                ((TextView) view.findViewById(R.id.sales_replace_qty)).setTextColor(Color.parseColor("#FFFFFF"));
                if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                        R.id.sales_replace_qty).getTag()) != null)
                    ((TextView) view.findViewById(R.id.sales_replace_qty))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(view.findViewById(
                                            R.id.sales_replace_qty).getTag()));
            } catch (Exception e) {
                Commons.printException(e + "");
            }
        }
        else {
            int totalReplaceQty = (productMasterBO.getRepCaseQty()*productMasterBO.getCaseSize())
                    +productMasterBO.getRepPieceQty()
                    +(productMasterBO.getRepOuterQty()*productMasterBO.getOutersize());
            ((TextView) view.findViewById(R.id.sales_replace_qty)).setText(String.valueOf(totalReplaceQty));
            ((TextView) view.findViewById(R.id.sales_replace_qty)).setTypeface(FontUtils.getFontRoboto(OrderDeliveryDetailActivity.this, FontUtils.FontType.LIGHT));
            ((TextView) view.findViewById(R.id.sales_replace_qty)).setTextColor(Color.parseColor("#000000"));
        }

        if(!isHeader){
            int totalOrderedQty = productMasterBO.getOrderedPcsQty() +
                    productMasterBO.getOrderedCaseQty()*productMasterBO.getCaseSize()+
                    productMasterBO.getOrderedOuterQty()*productMasterBO.getOutersize();

            int totalReplaceQty =  (productMasterBO.getRepCaseQty() * productMasterBO.getCaseSize())
                    +productMasterBO.getRepPieceQty()
                    +(productMasterBO.getRepOuterQty() * productMasterBO.getOutersize());


            if((totalOrderedQty+totalReplaceQty) > productMasterBO.getDSIH())
                ((TextView)view.findViewById(R.id.prod_name)).setTextColor(Color.parseColor("#FF0000"));
        }
    }

    @Override
    public void updateSchemeViewValues(ArrayList<SchemeProductBO> schemeProductBOS) {
        LayoutInflater inflater = LayoutInflater.from(this);

        if(schemeProductBOS.size() > 0){

            View cardView = inflater.inflate(R.layout.order_delivery_base_card_view, null);
            LinearLayout card_view_items = cardView.findViewById(R.id.card_view_items);

            View headerView = inflater.inflate(R.layout.order_delivery_scheme_item, null);
            setSchemeView(headerView,true,FontUtils.FontType.LIGHT,null);
            headerView.setBackgroundColor(Color.parseColor("#000000"));
            ViewGroup.LayoutParams headerParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.list_header_height));
            headerView.setLayoutParams(headerParams);

            card_view_items.addView(headerView);

            View childViews;
            for(SchemeProductBO schemeProductBO : schemeProductBOS){
                childViews = inflater.inflate(R.layout.order_delivery_scheme_item, null);
                setSchemeView(childViews,false,FontUtils.FontType.MEDIUM,schemeProductBO);
                ViewGroup.LayoutParams childParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.dimen_30dp));
                childViews.setLayoutParams(childParams);

                card_view_items.addView(childViews);
            }

            llContent.addView(cardView);
        }
    }

    private void setSchemeView(View view,boolean isHeader, FontUtils.FontType fontType,SchemeProductBO schemeProductBO){

        if(isHeader) {
            ((TextView)view.findViewById(R.id.prod_name)).setTypeface(FontUtils.getFontRoboto(OrderDeliveryDetailActivity.this, fontType));
            ((TextView) view.findViewById(R.id.prod_name)).setText(getResources().getString(R.string.free_products));
            ((TextView) view.findViewById(R.id.prod_name)).setTextColor(Color.parseColor("#FFFFFF"));
            ((TextView) view.findViewById(R.id.text_sih)).setText("");

        }
        else {
            ((TextView)view.findViewById(R.id.prod_name)).setTypeface(FontUtils.getFontRoboto(OrderDeliveryDetailActivity.this, fontType));
            ((TextView) view.findViewById(R.id.prod_name)).setText(schemeProductBO.getProductName());

            if(orderDeliveryPresenter.getRemainingStock(schemeProductBO.getProductId())>=schemeProductBO.getQuantitySelected())
            ((TextView) view.findViewById(R.id.prod_name)).setTextColor(Color.parseColor("#000000"));
            else ((TextView) view.findViewById(R.id.prod_name)).setTextColor(Color.parseColor("#FF0000"));
        }


        if(isHeader) {
            try {
                ((TextView) view.findViewById(R.id.piece_qty)).setText("");
                ((TextView) view.findViewById(R.id.piece_qty)).setTextColor(Color.parseColor("#FFFFFF"));
                ((TextView) view.findViewById(R.id.piece_qty)).setTypeface(FontUtils.getFontRoboto(OrderDeliveryDetailActivity.this, fontType));
                if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                        R.id.piece_qty).getTag()) != null)
                    ((TextView) view.findViewById(R.id.piece_qty))
                            .setText("");
            } catch (Exception e) {
                Commons.printException(e + "");
            }
        }
        else{
            ((TextView) view.findViewById(R.id.piece_qty)).setTypeface(FontUtils.getFontRoboto(OrderDeliveryDetailActivity.this, FontUtils.FontType.LIGHT));
            ((TextView) view.findViewById(R.id.piece_qty)).setTextColor(Color.parseColor("#000000"));
        }

        if(isHeader){
            try {
                ((TextView) view.findViewById(R.id.case_qty)).setText("");
                ((TextView) view.findViewById(R.id.case_qty)).setTypeface(FontUtils.getFontRoboto(OrderDeliveryDetailActivity.this, fontType));
                ((TextView) view.findViewById(R.id.case_qty)).setTextColor(Color.parseColor("#FFFFFF"));
                if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                        R.id.case_qty).getTag()) != null)
                    ((TextView) view.findViewById(R.id.case_qty))
                            .setText("");
            } catch (Exception e) {
                Commons.printException(e + "");
            }
        }
        else {
            ((TextView) view.findViewById(R.id.case_qty)).setTypeface(FontUtils.getFontRoboto(OrderDeliveryDetailActivity.this, FontUtils.FontType.LIGHT));
            ((TextView) view.findViewById(R.id.case_qty)).setTextColor(Color.parseColor("#000000"));
        }

        if (isHeader){
            try {
                ((TextView) view.findViewById(R.id.outer_qty)).setText("");
                ((TextView) view.findViewById(R.id.outer_qty)).setTypeface(FontUtils.getFontRoboto(OrderDeliveryDetailActivity.this, fontType));
                ((TextView) view.findViewById(R.id.outer_qty)).setTextColor(Color.parseColor("#FFFFFF"));
                if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                        R.id.outer_qty).getTag()) != null)
                    ((TextView) view.findViewById(R.id.outer_qty))
                            .setText("");
            } catch (Exception e) {
                Commons.printException(e + "");
            }
        }
        else {
            ((TextView) view.findViewById(R.id.outer_qty)).setTypeface(FontUtils.getFontRoboto(OrderDeliveryDetailActivity.this, FontUtils.FontType.LIGHT));
            ((TextView) view.findViewById(R.id.outer_qty)).setTextColor(Color.parseColor("#000000"));
        }

        if (!isHeader && bmodel.productHelper.getProductMasterBOById(schemeProductBO.getProductId())!=null) {
            if (schemeProductBO.getUomID() == bmodel.productHelper.getProductMasterBOById(schemeProductBO.getProductId()).getPcUomid())
                ((TextView) view.findViewById(R.id.piece_qty)).setText(String.valueOf(schemeProductBO.getQuantitySelected()));
            else if (schemeProductBO.getUomID() == bmodel.productHelper.getProductMasterBOById(schemeProductBO.getProductId()).getCaseUomId())
                ((TextView) view.findViewById(R.id.case_qty)).setText(String.valueOf(schemeProductBO.getQuantitySelected()));
            else if (schemeProductBO.getUomID() == bmodel.productHelper.getProductMasterBOById(schemeProductBO.getProductId()).getOuUomid())
                ((TextView) view.findViewById(R.id.outer_qty)).setText(String.valueOf(schemeProductBO.getQuantitySelected()));

            ((TextView) view.findViewById(R.id.text_sih)).setText(String.valueOf(bmodel.productHelper.getProductMasterBOById(schemeProductBO.getProductId()).getDSIH()));
        }

    }

    @Override
    public void updateAmountDetails(String orderVal, String discountAmt,String taxAmt, String totalOrderAmt) {
        totalOrderValue=SDUtil.convertToDouble(totalOrderAmt);
        orderBaseValue.setText(orderVal);
        discount_value.setText(discountAmt);
        taxValue.setText(taxAmt);
        orderValue.setText(totalOrderAmt);
    }

    @Override
    public void updateProductEditValues(Vector<ProductMasterBO> productList) {
        findViewById(R.id.scroll_view).setVisibility(View.GONE);
        findViewById(R.id.card_view).setVisibility(View.VISIBLE);
        findViewById(R.id.keypad).setVisibility(View.VISIBLE);
        initializeEditViewHeader();
        orderDeliveryPresenter.getAmountDetails(isEdit);
        MyAdapter myAdapter = new MyAdapter(productList);
        recyclerView.setAdapter(myAdapter);
        myAdapter.notifyDataSetChanged();
    }

    private void initializeEditViewHeader(){

        ((TextView)findViewById(R.id.prod_name)).setTypeface(FontUtils.getFontRoboto(OrderDeliveryDetailActivity.this, FontUtils.FontType.LIGHT));


        if (!bmodel.configurationMasterHelper.SHOW_DELIVERY_PC)
            (findViewById(R.id.piece_qty)).setVisibility(View.GONE);
        else {
            try {
                ((TextView) findViewById(R.id.piece_qty)).setTypeface(FontUtils.getFontRoboto(OrderDeliveryDetailActivity.this, FontUtils.FontType.LIGHT));
                if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                        R.id.piece_qty).getTag()) != null)
                    ((TextView) findViewById(R.id.piece_qty))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(findViewById(
                                            R.id.piece_qty).getTag()));
            } catch (Exception e) {
                Commons.printException(e + "");
            }
        }

        if (!bmodel.configurationMasterHelper.SHOW_DELIVERY_CA)
            ( findViewById(R.id.case_qty)).setVisibility(View.GONE);
        else {
            try {
                ((TextView) findViewById(R.id.case_qty)).setTypeface(FontUtils.getFontRoboto(OrderDeliveryDetailActivity.this, FontUtils.FontType.LIGHT));
                if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                        R.id.case_qty).getTag()) != null)
                    ((TextView) findViewById(R.id.case_qty))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(findViewById(
                                            R.id.case_qty).getTag()));
            } catch (Exception e) {
                Commons.printException(e + "");
            }
        }

        if (!bmodel.configurationMasterHelper.SHOW_DELIVERY_OU)
            (findViewById(R.id.outer_qty)).setVisibility(View.GONE);
        else {
            try {
                ((TextView) findViewById(R.id.outer_qty)).setTypeface(FontUtils.getFontRoboto(OrderDeliveryDetailActivity.this, FontUtils.FontType.LIGHT));
                if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                        R.id.outer_qty).getTag()) != null)
                    ((TextView) findViewById(R.id.outer_qty))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(findViewById(
                                            R.id.outer_qty).getTag()));
            } catch (Exception e) {
                Commons.printException(e + "");
            }
        }

        if (!bmodel.configurationMasterHelper.IS_STOCK_IN_HAND)
            (findViewById(R.id.sih_qty)).setVisibility(View.GONE);
        else {
            try {
                ((TextView) findViewById(R.id.sih_qty)).setTypeface(FontUtils.getFontRoboto(OrderDeliveryDetailActivity.this, FontUtils.FontType.LIGHT));
                if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                        R.id.sih_qty).getTag()) != null)
                    ((TextView) findViewById(R.id.sih_qty))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(findViewById(
                                            R.id.sih_qty).getTag()));
            } catch (Exception e) {
                Commons.printException(e + "");
            }
        }

        if (!bmodel.configurationMasterHelper.SHOW_SALES_RETURN_IN_DELIVERY)
            (findViewById(R.id.sales_return_qty)).setVisibility(View.GONE);
        else {
            try {
                ((TextView) findViewById(R.id.sales_return_qty)).setTypeface(FontUtils.getFontRoboto(OrderDeliveryDetailActivity.this, FontUtils.FontType.LIGHT));
                if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                        R.id.sales_return_qty).getTag()) != null)
                    ((TextView) findViewById(R.id.sales_return_qty))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(findViewById(
                                            R.id.sales_return_qty).getTag()));
            } catch (Exception e) {
                Commons.printException(e + "");
            }
        }

        if (!bmodel.configurationMasterHelper.SHOW_SALES_RETURN_IN_DELIVERY)
            (findViewById(R.id.sales_replace_qty)).setVisibility(View.GONE);
        else {
            try {
                ((TextView) findViewById(R.id.sales_replace_qty)).setTypeface(FontUtils.getFontRoboto(OrderDeliveryDetailActivity.this, FontUtils.FontType.LIGHT));
                if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                        R.id.sales_replace_qty).getTag()) != null)
                    ((TextView) findViewById(R.id.sales_replace_qty))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(findViewById(
                                            R.id.sales_replace_qty).getTag()));
            } catch (Exception e) {
                Commons.printException(e + "");
            }
        }
    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

        private Vector<ProductMasterBO> productList;

        MyAdapter(Vector<ProductMasterBO> productList){
            this.productList = productList;
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            private TextView productName,srQty,sihQty,srpQty;
            private EditText pieceQty,caseQty,outerQty;

            public MyViewHolder(View view) {
                super(view);

                productName = view.findViewById(R.id.prod_name);
                pieceQty = view.findViewById(R.id.piece_qty);
                caseQty = view.findViewById(R.id.case_qty);
                outerQty = view.findViewById(R.id.outer_qty);
                srQty = view.findViewById(R.id.sales_return_qty);
                sihQty = view.findViewById(R.id.sih_qty);
                srpQty = view.findViewById(R.id.sales_replace_qty);

                productName.setTypeface(FontUtils.getFontRoboto(OrderDeliveryDetailActivity.this, FontUtils.FontType.MEDIUM));

                if (!bmodel.configurationMasterHelper.SHOW_DELIVERY_PC)
                    pieceQty.setVisibility(View.GONE);
                else {
                    try {
                        pieceQty.setTypeface(FontUtils.getFontRoboto(OrderDeliveryDetailActivity.this, FontUtils.FontType.LIGHT));
                        if (bmodel.labelsMasterHelper.applyLabels(pieceQty.getTag()) != null)
                            pieceQty.setText(bmodel.labelsMasterHelper
                                    .applyLabels(pieceQty.getTag()));
                    } catch (Exception e) {
                        Commons.printException(e + "");
                    }
                }

                if (!bmodel.configurationMasterHelper.SHOW_DELIVERY_CA)
                    caseQty.setVisibility(View.GONE);
                else {
                    try {
                        caseQty.setTypeface(FontUtils.getFontRoboto(OrderDeliveryDetailActivity.this, FontUtils.FontType.LIGHT));
                        if (bmodel.labelsMasterHelper.applyLabels(caseQty.getTag()) != null)
                            caseQty.setText(bmodel.labelsMasterHelper
                                    .applyLabels(caseQty.getTag()));
                    } catch (Exception e) {
                        Commons.printException(e + "");
                    }
                }

                if (!bmodel.configurationMasterHelper.SHOW_DELIVERY_OU)
                    outerQty.setVisibility(View.GONE);
                else {
                    try {
                        outerQty.setTypeface(FontUtils.getFontRoboto(OrderDeliveryDetailActivity.this, FontUtils.FontType.LIGHT));
                        if (bmodel.labelsMasterHelper.applyLabels(outerQty.getTag()) != null)
                            outerQty.setText(bmodel.labelsMasterHelper
                                    .applyLabels(outerQty.getTag()));
                    } catch (Exception e) {
                        Commons.printException(e + "");
                    }
                }

                if (!bmodel.configurationMasterHelper.IS_STOCK_IN_HAND)
                    sihQty.setVisibility(View.GONE);
                else {
                    try {
                        sihQty.setTypeface(FontUtils.getFontRoboto(OrderDeliveryDetailActivity.this, FontUtils.FontType.LIGHT));
                        if (bmodel.labelsMasterHelper.applyLabels(sihQty.getTag()) != null)
                            sihQty.setText(bmodel.labelsMasterHelper
                                    .applyLabels(sihQty.getTag()));
                    } catch (Exception e) {
                        Commons.printException(e + "");
                    }
                }

                if (!bmodel.configurationMasterHelper.SHOW_SALES_RETURN_IN_DELIVERY)
                    srpQty.setVisibility(View.GONE);
                else {
                    try {
                        srpQty.setTypeface(FontUtils.getFontRoboto(OrderDeliveryDetailActivity.this, FontUtils.FontType.LIGHT));
                        if (bmodel.labelsMasterHelper.applyLabels(srpQty.getTag()) != null)
                            srpQty.setText(bmodel.labelsMasterHelper
                                    .applyLabels(srpQty.getTag()));
                    } catch (Exception e) {
                        Commons.printException(e + "");
                    }
                }

                if (!bmodel.configurationMasterHelper.SHOW_SALES_RETURN_IN_DELIVERY)
                    srQty.setVisibility(View.GONE);
                else {
                    try {
                        srQty.setTypeface(FontUtils.getFontRoboto(OrderDeliveryDetailActivity.this, FontUtils.FontType.LIGHT));
                        if (bmodel.labelsMasterHelper.applyLabels(srQty.getTag()) != null)
                            srQty.setText(bmodel.labelsMasterHelper
                                    .applyLabels(srQty.getTag()));
                    } catch (Exception e) {
                        Commons.printException(e + "");
                    }
                }

            }
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.order_delivery_edit_detail_item, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {

            holder.productName.setTextColor(Color.parseColor("#000000"));
            holder.pieceQty.setTextColor(Color.parseColor("#000000"));
            holder.caseQty.setTextColor(Color.parseColor("#000000"));
            holder.outerQty.setTextColor(Color.parseColor("#000000"));
            holder.sihQty.setTextColor(Color.parseColor("#000000"));
            holder.srpQty.setTextColor(Color.parseColor("#000000"));
            holder.srQty.setTextColor(Color.parseColor("#000000"));


            holder.productName.setText(String.valueOf(productList.get(position).getProductName()));
            holder.pieceQty.setText(String.valueOf(productList.get(position).getOrderedPcsQty()));
            holder.caseQty.setText(String.valueOf(productList.get(position).getOrderedCaseQty()));
            holder.outerQty.setText(String.valueOf(productList.get(position).getOrderedOuterQty()));

            int totalOrderedQty = productList.get(position).getOrderedPcsQty() +
                    productList.get(position).getOrderedCaseQty()*productList.get(position).getCaseSize()+
                    productList.get(position).getOrderedOuterQty()*productList.get(position).getOutersize();

            int totalReplaceQty =  (productList.get(position).getRepCaseQty() * productList.get(position).getCaseSize())
                    +productList.get(position).getRepPieceQty()
                    +(productList.get(position).getRepOuterQty() * productList.get(position).getOutersize());


            if((totalOrderedQty+totalReplaceQty) > productList.get(position).getDSIH())
                holder.productName.setTextColor(Color.parseColor("#FF0000"));


            holder.pieceQty.setTag(String.valueOf(totalOrderedQty));
            holder.caseQty.setTag(String.valueOf(totalOrderedQty));
            holder.outerQty.setTag(String.valueOf(totalOrderedQty));

            holder.sihQty.setText(String.valueOf(productList.get(position).getDSIH()));

            if (bmodel.configurationMasterHelper.SHOW_SALES_RETURN_IN_DELIVERY) {
                int total = 0;
                if (productList.get(position).getSalesReturnReasonList() != null) {
                    for (SalesReturnReasonBO obj : productList.get(position).getSalesReturnReasonList())
                        total = total + obj.getPieceQty() + (obj.getCaseQty() * obj.getCaseSize()) + (obj.getOuterQty() * obj.getOuterSize());
                }
                String strTotal = Integer.toString(total);
                holder.srQty.setText(strTotal);
            }


            holder.srpQty.setText(String.valueOf(totalReplaceQty));

            if (productList.get(position).getOuUomid() == 0 || !productList.get(position).isOuterMapped()) {
                holder.outerQty.setEnabled(false);
            } else {
                holder.outerQty.setEnabled(true);
            }
            if (productList.get(position).getCaseUomId() == 0 || !productList.get(position).isCaseMapped()) {
                holder.caseQty.setEnabled(false);
            } else {
                holder.caseQty.setEnabled(true);
            }
            if (productList.get(position).getPcUomid() == 0 || !productList.get(position).isPieceMapped()) {
                holder.pieceQty.setEnabled(false);
            } else {
                holder.pieceQty.setEnabled(true);
            }


            holder.pieceQty.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                    if (productList.get(position).getPcUomid() == 0) {
                        holder.pieceQty.removeTextChangedListener(this);
                        holder.pieceQty.setText("0");
                        holder.pieceQty.addTextChangedListener(this);
                        return;
                    }

                    String qty = s.toString();
                    float totalQty = (productList.get(position).getOrderedCaseQty() * productList.get(position).getCaseSize())
                            + (SDUtil.convertToInt(qty))
                            + (productList.get(position).getOrderedOuterQty() * productList.get(position).getOutersize())
                            + productList.get(position).getRepPieceQty()
                            + (productList.get(position).getRepCaseQty()*productList.get(position).getCaseSize())
                            + (productList.get(position).getRepOuterQty()*productList.get(position).getOutersize());

                    int storedPieceQty = 0 ;
                    if(holder.pieceQty.getTag()!=null && !holder.pieceQty.getTag().toString().equals(""))
                        storedPieceQty = Integer.valueOf(holder.pieceQty.getTag().toString());

                    int currentOrderedQty = (productList.get(position).getOrderedCaseQty() * productList.get(position).getCaseSize())
                            + (SDUtil.convertToInt(qty))
                            + (productList.get(position).getOrderedOuterQty() * productList.get(position).getOutersize());
                    if(currentOrderedQty==0) {
                        //Clearing taxable amount, so that it will not shown in print
                        productList.get(position).setTaxableAmount(0);
                    }

                    if (totalQty <= productList.get(position).getDSIH() &&
                            currentOrderedQty <= storedPieceQty ) {
                        if (!"".equals(qty)) {
                            productList.get(position).setOrderedPcsQty(SDUtil
                                    .convertToInt(qty));
                        }
                        double tot = (productList.get(position).getOrderedCaseQty() * productList.get(position).getCsrp())
                                + (productList.get(position).getOrderedPcsQty() * productList.get(position).getSrp())
                                + (productList.get(position).getOrderedOuterQty() * productList.get(position).getOsrp());

                        productList.get(position).setTotalamount(tot);
                        orderDeliveryPresenter.getAmountDetails(isEdit);
                        holder.productName.setTextColor(Color.parseColor("#000000"));
                    } else {
                        if (!"0".equals(qty)) {

                            if(currentOrderedQty > storedPieceQty){
                                Toast.makeText(
                                        OrderDeliveryDetailActivity.this,
                                        getResources().getString(
                                                R.string.exceed_ordered_value),
                                        Toast.LENGTH_SHORT).show();

                                //Delete the last entered number and reset the qty
                                qty = qty.length() > 1 ? qty.substring(0,
                                        qty.length() - 1) : "0";
                                holder.pieceQty.setText(qty);
                            }
                            else if (totalQty > productList.get(position).getDSIH()) {
                                holder.productName.setTextColor(Color.parseColor("#FF0000"));
//                                Toast.makeText(
//                                        OrderDeliveryDetailActivity.this,
//                                        String.format(
//                                                getResources().getString(
//                                                        R.string.exceed),
//                                                productList.get(position).getDSIH()),
//                                        Toast.LENGTH_SHORT).show();
                            }


                            productList.get(position).setOrderedPcsQty(SDUtil
                                    .convertToInt(qty));
                            orderDeliveryPresenter.getAmountDetails(isEdit);

                        }
                    }

                }

                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                }
            });

            holder.pieceQty.setFocusable(true);
            holder.pieceQty.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {

                    QUANTITY = holder.pieceQty;
                    int inType = holder.pieceQty.getInputType();
                    holder.pieceQty.setInputType(InputType.TYPE_NULL);
                    holder.pieceQty.onTouchEvent(event);
                    holder.pieceQty.setInputType(inType);
                    holder.pieceQty.selectAll();
                    holder.pieceQty.requestFocus();
                    inputManager.hideSoftInputFromWindow(
                            holder.pieceQty.getWindowToken(), 0);
                    return true;
                }
                });



            holder.caseQty.addTextChangedListener(new TextWatcher() {
                @SuppressLint("StringFormatInvalid")
                public void afterTextChanged(Editable s) {
                    if (productList.get(position).getCaseSize() == 0) {
                        holder.caseQty.removeTextChangedListener(this);
                        holder.caseQty.setText("0");
                        holder.caseQty.addTextChangedListener(this);
                        return;
                    }

                    String qty = s.toString();

                    float totalQty = (SDUtil.convertToInt(qty) * productList.get(position).getCaseSize())
                            + (productList.get(position).getOrderedPcsQty())
                            + (productList.get(position).getOrderedOuterQty() * productList.get(position).getOutersize())
                            + productList.get(position).getRepPieceQty()
                            + (productList.get(position).getRepCaseQty()*productList.get(position).getCaseSize())
                            + (productList.get(position).getRepOuterQty()*productList.get(position).getOutersize());

                    int storedcaseQty = 0 ;
                    if(holder.caseQty.getTag()!=null && !holder.caseQty.getTag().toString().equals(""))
                        storedcaseQty = Integer.valueOf(holder.caseQty.getTag().toString());

                    int currentOrderedQty = (SDUtil.convertToInt(qty) * productList.get(position).getCaseSize())
                            + (productList.get(position).getOrderedPcsQty())
                            + (productList.get(position).getOrderedOuterQty() * productList.get(position).getOutersize());
                    if(currentOrderedQty==0) {
                        //Clearing taxable amount, so that it will not shown in print
                        productList.get(position).setTaxableAmount(0);
                    }

                    if (totalQty <= productList.get(position).getDSIH() &&
                            currentOrderedQty <= storedcaseQty) {
                        if (!"".equals(qty)) {
                            productList.get(position).setOrderedCaseQty(SDUtil
                                    .convertToInt(qty));
                        }

                        double tot = (productList.get(position).getOrderedCaseQty() * productList.get(position).getCsrp())
                                + (productList.get(position).getOrderedPcsQty() * productList.get(position).getSrp())
                                + (productList.get(position).getOrderedOuterQty() * productList.get(position).getOsrp());
                        productList.get(position).setTotalamount(tot);
                        orderDeliveryPresenter.getAmountDetails(isEdit);
                        holder.productName.setTextColor(Color.parseColor("#000000"));
                    } else {
                        if (!"0".equals(qty)) {

                            if(currentOrderedQty > storedcaseQty){
                                Toast.makeText(
                                        OrderDeliveryDetailActivity.this,
                                        getResources().getString(
                                                R.string.exceed_ordered_value),
                                        Toast.LENGTH_SHORT).show();

                                //Delete the last entered number and reset the qty
                                qty = qty.length() > 1 ? qty.substring(0,
                                        qty.length() - 1) : "0";
                                holder.caseQty.setText(qty);

                            }
                            else if (totalQty > productList.get(position).getDSIH()) {
                                holder.productName.setTextColor(Color.parseColor("#FF0000"));
//                                Toast.makeText(
//                                        OrderDeliveryDetailActivity.this,
//                                        String.format(
//                                                getResources().getString(
//                                                        R.string.exceed),
//                                                productList.get(position).getDSIH()),
//                                        Toast.LENGTH_SHORT).show();
                            }


                            productList.get(position).setOrderedCaseQty(SDUtil
                                    .convertToInt(qty));
                            orderDeliveryPresenter.getAmountDetails(isEdit);
                        }
                    }
                }

                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                }
            });


            holder.caseQty.setFocusable(true);
            holder.caseQty.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {

                    QUANTITY = holder.caseQty;
                    int inType = holder.caseQty.getInputType();
                    holder.caseQty.setInputType(InputType.TYPE_NULL);
                    holder.caseQty.onTouchEvent(event);
                    holder.caseQty.setInputType(inType);
                    holder.caseQty.selectAll();
                    holder.caseQty.requestFocus();
                    inputManager.hideSoftInputFromWindow(
                            holder.caseQty.getWindowToken(), 0);
                    return true;
                }
            });

            holder.outerQty.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    if (productList.get(position).getOuUomid() == 0) {
                        holder.outerQty.removeTextChangedListener(this);
                        holder.outerQty.setText("0");
                        holder.outerQty.addTextChangedListener(this);
                        return;
                    }
                    String qty = s.toString();

                    float totalQty = (SDUtil.convertToInt(qty) * productList.get(position).getOutersize())
                            + (productList.get(position).getOrderedCaseQty() * productList.get(position).getCaseSize())
                            + (productList.get(position).getOrderedPcsQty())
                            + productList.get(position).getRepPieceQty()
                            + (productList.get(position).getRepCaseQty()*productList.get(position).getCaseSize())
                            + (productList.get(position).getRepOuterQty()*productList.get(position).getOutersize());


                    int storedouterQty = 0 ;
                    if(holder.outerQty.getTag()!=null && !holder.outerQty.getTag().toString().equals(""))
                        storedouterQty = Integer.valueOf(holder.outerQty.getTag().toString());

                    int currentOrderedQty = (SDUtil.convertToInt(qty) * productList.get(position).getOutersize())
                            + (productList.get(position).getOrderedCaseQty() * productList.get(position).getCaseSize())
                            + (productList.get(position).getOrderedPcsQty());
                    if(currentOrderedQty==0) {
                        //Clearing taxable amount, so that it will not shown in print
                        productList.get(position).setTaxableAmount(0);
                    }

                    if (totalQty <= productList.get(position).getDSIH() &&
                            currentOrderedQty <= storedouterQty) {
                        if (!"".equals(qty)) {
                            productList.get(position).setOrderedOuterQty(SDUtil
                                    .convertToInt(qty));
                        }

                        double tot = (productList.get(position).getOrderedCaseQty() * productList.get(position).getCsrp())
                                + (productList.get(position).getOrderedPcsQty() * productList.get(position).getSrp())
                                + (productList.get(position).getOrderedOuterQty() * productList.get(position).getOsrp());
                        productList.get(position).setTotalamount(tot);
                        orderDeliveryPresenter.getAmountDetails(isEdit);
                        holder.productName.setTextColor(Color.parseColor("#000000"));
                    } else {
                        if (!"0".equals(qty)) {

                            if(currentOrderedQty > storedouterQty){
                                Toast.makeText(
                                        OrderDeliveryDetailActivity.this,
                                        getResources().getString(
                                                R.string.exceed_ordered_value),
                                        Toast.LENGTH_SHORT).show();

                                qty = qty.length() > 1 ? qty.substring(0,
                                        qty.length() - 1) : "0";
                                holder.outerQty.setText(qty);
                            }
                            if (totalQty > productList.get(position).getDSIH()) {
                                holder.productName.setTextColor(Color.parseColor("#FF0000"));
//                                Toast.makeText(
//                                        OrderDeliveryDetailActivity.this,
//                                        String.format(
//                                                getResources().getString(
//                                                        R.string.exceed),
//                                                productList.get(position).getDSIH()),
//                                        Toast.LENGTH_SHORT).show();
                            }



                            productList.get(position).setOrderedOuterQty(SDUtil
                                    .convertToInt(qty));
                            orderDeliveryPresenter.getAmountDetails(isEdit);


                        }
                    }

                }

                @Override
                public void afterTextChanged(Editable s) {
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

            });

            holder.outerQty.setFocusable(true);
            holder.outerQty.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {

                    QUANTITY = holder.outerQty;
                    int inType = holder.outerQty.getInputType();
                    holder.outerQty.setInputType(InputType.TYPE_NULL);
                    holder.outerQty.onTouchEvent(event);
                    holder.outerQty.setInputType(inType);
                    holder.outerQty.selectAll();
                    holder.outerQty.requestFocus();
                    inputManager.hideSoftInputFromWindow(
                            holder.outerQty.getWindowToken(), 0);
                    return true;
                }
            });
        }

        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return productList.size();
        }
    }

    @Override
    public void updateSaveStatus(boolean isSuccess) {
        if (isSuccess) {

            bmodel.outletTimeStampHelper.updateTimeStampModuleWise(DateTimeUtils
                    .now(DateTimeUtils.TIME));

            Intent i = new Intent(this,
                    CommonPrintPreviewActivity.class);
            i.putExtra("IsFromOrder", false);
            i.putExtra("IsUpdatePrintCount", true);
            i.putExtra("isHomeBtnEnable", true);
            i.putExtra("sendMailAndLoadClass", "PRINT_FILE_INVOICE");
            startActivity(i);
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            finish();
        }
    }

    @Override
    public void updatePrintStatus(final String msg,boolean status) {
        try {
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(OrderDeliveryDetailActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Commons.printException(e);
        }

        if(status)
            isPrintClicked = status;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_print_preview, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        menu.findItem(R.id.menu_share_pdf).setVisible(false);
        menu.findItem(R.id.menu_email_print).setVisible(false);

        if(invoiceStatus == 1)
            menu.findItem(R.id.menu_print).setVisible(true);
        else
            menu.findItem(R.id.menu_print).setVisible(false);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            backButtonClick();
            return true;
        }
        else if(i ==  R.id.menu_print) {
            if (!isPrintClicked) {
                orderDeliveryPresenter.doPrintActivity(orderId);
            }
        }


        return super.onOptionsItemSelected(item);
    }

    private void backButtonClick() {
        try {

           finish();
           overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);

        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private void eff() {
        String s = QUANTITY.getText().toString();
        if (!"0".equals(s) && !"0.0".equals(s)) {
            String strQuantity = QUANTITY.getText() + append;
            QUANTITY.setText(strQuantity);
        } else
            QUANTITY.setText(append);
    }

    public void numberPressed(View vw) {

        int val;
        if (QUANTITY == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.please_select_item), 0);
        } else {
            int id = vw.getId();
            if (id == R.id.calcdel) {

                int s = SDUtil.convertToInt(QUANTITY.getText()
                        .toString());
                s = s / 10;
                String strS = s + "";
                QUANTITY.setText(strS);
                val = s;


            } else if (id == R.id.calcdot) {
                val = SDUtil.convertToInt(append);
            } else {
                Button ed = findViewById(vw.getId());
                append = ed.getText().toString();
                eff();
                val = SDUtil.convertToInt(append);
            }
        }

    }

}
