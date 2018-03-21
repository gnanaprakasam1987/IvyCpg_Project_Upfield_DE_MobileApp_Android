package com.ivy.cpg.view.orderdelivery;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.cpg.view.salesreturn.SalesReturnReasonBO;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.SchemeProductBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.CustomKeyBoard;

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

        initializeViews();

        orderDeliveryPresenter = new OrderDeliveryPresenterImpl(this,bmodel);
        orderDeliveryPresenter.setView(this);

        orderDeliveryPresenter.getProductData(getIntent().getExtras().getString("From"));

        if(getIntent().getExtras().getString("From").equals("ViewDetail")){
            orderDeliveryPresenter.getSchemeData();
        }

        orderDeliveryPresenter.getAmountDetails();
    }

    private void initializeViews(){

        recyclerView = findViewById(R.id.order_detail_product_list);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        llContent = findViewById(R.id.ll_content);

        findViewById(R.id.accept_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                orderDeliveryPresenter.saveOrderDeliveryDetail(
                        getIntent().getExtras().getString("From").equalsIgnoreCase("Edit")?true:false,
                        getIntent().getExtras().getString("OrderId")
                );
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
            setProductView(headerView,true,ConfigurationMasterHelper.FontType.LIGHT,null);
            headerView.setBackgroundColor(Color.parseColor("#000000"));

            ViewGroup.LayoutParams headerParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.list_header_height));
            headerView.setLayoutParams(headerParams);

            card_view_items.addView(headerView);

            View childViews;
            for(ProductMasterBO productMasterBO : productList){
                childViews = inflater.inflate(R.layout.order_delivery_detail_item, null);
                setProductView(childViews,false,ConfigurationMasterHelper.FontType.MEDIUM,productMasterBO);

                ViewGroup.LayoutParams childParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.dimens_50dp));
                childViews.setLayoutParams(childParams);

                card_view_items.addView(childViews);
            }

            llContent.addView(cardView);


        }


    }

    @Override
    public void updateProductEditValues(Vector<ProductMasterBO> productList) {
        findViewById(R.id.scroll_view).setVisibility(View.GONE);
        findViewById(R.id.card_view).setVisibility(View.VISIBLE);
        findViewById(R.id.keypad).setVisibility(View.VISIBLE);
        initializeEditViewHeader();
        MyAdapter myAdapter = new MyAdapter(productList);
        recyclerView.setAdapter(myAdapter);
        myAdapter.notifyDataSetChanged();
    }

    private void initializeEditViewHeader(){

        ((TextView)findViewById(R.id.prod_name)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));


        if (!bmodel.configurationMasterHelper.SHOW_ORDER_PCS)
            (findViewById(R.id.piece_qty)).setVisibility(View.GONE);
        else {
            try {
                ((TextView) findViewById(R.id.piece_qty)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
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

        if (!bmodel.configurationMasterHelper.SHOW_ORDER_CASE)
            ( findViewById(R.id.case_qty)).setVisibility(View.GONE);
        else {
            try {
                ((TextView) findViewById(R.id.case_qty)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
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

        if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE)
            (findViewById(R.id.outer_qty)).setVisibility(View.GONE);
        else {
            try {
                ((TextView) findViewById(R.id.outer_qty)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
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
                ((TextView) findViewById(R.id.sih_qty)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
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

        if (!bmodel.configurationMasterHelper.SHOW_SALES_RETURN_IN_ORDER)
            (findViewById(R.id.sales_return_qty)).setVisibility(View.GONE);
        else {
            try {
                ((TextView) findViewById(R.id.sales_return_qty)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
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

        if (!bmodel.configurationMasterHelper.SHOW_SALES_RETURN_IN_ORDER)
            (findViewById(R.id.sales_replace_qty)).setVisibility(View.GONE);
        else {
            try {
                ((TextView) findViewById(R.id.sales_replace_qty)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
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

    private void setProductView(View view,boolean isHeader, ConfigurationMasterHelper.FontType fontType,ProductMasterBO productMasterBO){

        if(isHeader) {
            ((TextView)view.findViewById(R.id.prod_name)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(fontType));
            ((TextView) view.findViewById(R.id.prod_name)).setText(getResources().getString(R.string.product_name));
            ((TextView) view.findViewById(R.id.prod_name)).setTextColor(Color.parseColor("#FFFFFF"));
        }
        else {
            ((TextView)view.findViewById(R.id.prod_name)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(fontType));
            ((TextView) view.findViewById(R.id.prod_name)).setText(productMasterBO.getProductName());
            ((TextView) view.findViewById(R.id.prod_name)).setTextColor(Color.parseColor("#000000"));
        }


        if (!bmodel.configurationMasterHelper.SHOW_ORDER_PCS)
            (view.findViewById(R.id.piece_qty)).setVisibility(View.GONE);
        else if(isHeader) {
            try {
                ((TextView) view.findViewById(R.id.piece_qty)).setText(getResources().getString(R.string.piece));
                ((TextView) view.findViewById(R.id.piece_qty)).setTextColor(Color.parseColor("#FFFFFF"));
                ((TextView) view.findViewById(R.id.piece_qty)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(fontType));
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
            ((TextView) view.findViewById(R.id.piece_qty)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            ((TextView) view.findViewById(R.id.piece_qty)).setTextColor(Color.parseColor("#000000"));
        }

        if (!bmodel.configurationMasterHelper.SHOW_ORDER_CASE)
            ( view.findViewById(R.id.case_qty)).setVisibility(View.GONE);
        else if(isHeader){
            try {
                ((TextView) view.findViewById(R.id.case_qty)).setText(getResources().getString(R.string.case_u));
                ((TextView) view.findViewById(R.id.case_qty)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(fontType));
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
            ((TextView) view.findViewById(R.id.case_qty)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            ((TextView) view.findViewById(R.id.case_qty)).setTextColor(Color.parseColor("#000000"));
        }

        if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE)
            (view.findViewById(R.id.outer_qty)).setVisibility(View.GONE);
        else if (isHeader){
            try {
                ((TextView) view.findViewById(R.id.outer_qty)).setText(getResources().getString(R.string.outer_label));
                ((TextView) view.findViewById(R.id.outer_qty)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(fontType));
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
            ((TextView) view.findViewById(R.id.outer_qty)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            ((TextView) view.findViewById(R.id.outer_qty)).setTextColor(Color.parseColor("#000000"));
        }

        if (!bmodel.configurationMasterHelper.IS_STOCK_IN_HAND)
            (view.findViewById(R.id.sih_qty)).setVisibility(View.GONE);
        else if (isHeader){
            try {
                ((TextView) view.findViewById(R.id.sih_qty)).setText(getResources().getString(R.string.sih));
                ((TextView) view.findViewById(R.id.sih_qty)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(fontType));
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
            ((TextView) view.findViewById(R.id.sih_qty)).setText(String.valueOf(productMasterBO.getSIH()));
            ((TextView) view.findViewById(R.id.sih_qty)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            ((TextView) view.findViewById(R.id.sih_qty)).setTextColor(Color.parseColor("#000000"));
        }

        if (!bmodel.configurationMasterHelper.SHOW_SALES_RETURN_IN_ORDER)
            (view.findViewById(R.id.sales_return_qty)).setVisibility(View.GONE);
        else if (isHeader){
            try {
                ((TextView) view.findViewById(R.id.sales_return_qty)).setText(getResources().getString(R.string.sr));
                ((TextView) view.findViewById(R.id.sales_return_qty)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(fontType));
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
                for (SalesReturnReasonBO obj : productMasterBO.getSalesReturnReasonList())
                    total = total + obj.getPieceQty() + (obj.getCaseQty() * obj.getCaseSize()) + (obj.getOuterQty() * obj.getOuterSize());
            }
            ((TextView) view.findViewById(R.id.sales_return_qty)).setText(String.valueOf(total));
            ((TextView) view.findViewById(R.id.sales_return_qty)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            ((TextView) view.findViewById(R.id.sales_return_qty)).setTextColor(Color.parseColor("#000000"));
        }

        if (!bmodel.configurationMasterHelper.SHOW_SALES_RETURN_IN_ORDER)
            (view.findViewById(R.id.sales_replace_qty)).setVisibility(View.GONE);
        else if (isHeader){
            try {
                ((TextView) view.findViewById(R.id.sales_replace_qty)).setText(getResources().getString(R.string.replacement));
                ((TextView) view.findViewById(R.id.sales_replace_qty)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(fontType));
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
            ((TextView) view.findViewById(R.id.sales_replace_qty)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            ((TextView) view.findViewById(R.id.sales_replace_qty)).setTextColor(Color.parseColor("#000000"));
        }
    }

    @Override
    public void updateSchemeViewValues(ArrayList<SchemeProductBO> schemeProductBOS) {
        LayoutInflater inflater = LayoutInflater.from(this);

        if(schemeProductBOS.size() > 0){

            View cardView = inflater.inflate(R.layout.order_delivery_base_card_view, null);
            LinearLayout card_view_items = cardView.findViewById(R.id.card_view_items);

            View headerView = inflater.inflate(R.layout.order_delivery_scheme_item, null);
            setSchemeView(headerView,true,ConfigurationMasterHelper.FontType.LIGHT,null);
            headerView.setBackgroundColor(Color.parseColor("#000000"));
            ViewGroup.LayoutParams headerParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.list_header_height));
            headerView.setLayoutParams(headerParams);

            card_view_items.addView(headerView);

            View childViews;
            for(SchemeProductBO schemeProductBO : schemeProductBOS){
                childViews = inflater.inflate(R.layout.order_delivery_scheme_item, null);
                setSchemeView(childViews,false,ConfigurationMasterHelper.FontType.MEDIUM,schemeProductBO);
                ViewGroup.LayoutParams childParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.dimen_30dp));
                childViews.setLayoutParams(childParams);

                card_view_items.addView(childViews);
            }

            llContent.addView(cardView);
        }
    }

    private void setSchemeView(View view,boolean isHeader, ConfigurationMasterHelper.FontType fontType,SchemeProductBO schemeProductBO){

        if(isHeader) {
            ((TextView)view.findViewById(R.id.prod_name)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(fontType));
            ((TextView) view.findViewById(R.id.prod_name)).setText(getResources().getString(R.string.free_products));
            ((TextView) view.findViewById(R.id.prod_name)).setTextColor(Color.parseColor("#FFFFFF"));
        }
        else {
            ((TextView)view.findViewById(R.id.prod_name)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(fontType));
            ((TextView) view.findViewById(R.id.prod_name)).setText(schemeProductBO.getProductName());
            ((TextView) view.findViewById(R.id.prod_name)).setTextColor(Color.parseColor("#000000"));
        }


        if(isHeader) {
            try {
                ((TextView) view.findViewById(R.id.piece_qty)).setText("");
                ((TextView) view.findViewById(R.id.piece_qty)).setTextColor(Color.parseColor("#FFFFFF"));
                ((TextView) view.findViewById(R.id.piece_qty)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(fontType));
                if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                        R.id.piece_qty).getTag()) != null)
                    ((TextView) view.findViewById(R.id.piece_qty))
                            .setText("");
            } catch (Exception e) {
                Commons.printException(e + "");
            }
        }
        else{
            ((TextView) view.findViewById(R.id.piece_qty)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            ((TextView) view.findViewById(R.id.piece_qty)).setTextColor(Color.parseColor("#000000"));
        }

        if(isHeader){
            try {
                ((TextView) view.findViewById(R.id.case_qty)).setText("");
                ((TextView) view.findViewById(R.id.case_qty)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(fontType));
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
            ((TextView) view.findViewById(R.id.case_qty)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            ((TextView) view.findViewById(R.id.case_qty)).setTextColor(Color.parseColor("#000000"));
        }

        if (isHeader){
            try {
                ((TextView) view.findViewById(R.id.outer_qty)).setText("");
                ((TextView) view.findViewById(R.id.outer_qty)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(fontType));
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
            ((TextView) view.findViewById(R.id.outer_qty)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            ((TextView) view.findViewById(R.id.outer_qty)).setTextColor(Color.parseColor("#000000"));
        }

        if (!isHeader) {
            if (schemeProductBO.getUomID() == bmodel.productHelper.getProductMasterBOById(schemeProductBO.getProductId()).getPcUomid())
                ((TextView) view.findViewById(R.id.piece_qty)).setText(String.valueOf(schemeProductBO.getQuantitySelected()));
            else if (schemeProductBO.getUomID() == bmodel.productHelper.getProductMasterBOById(schemeProductBO.getProductId()).getCaseUomId())
                ((TextView) view.findViewById(R.id.case_qty)).setText(String.valueOf(schemeProductBO.getQuantitySelected()));
            else if (schemeProductBO.getUomID() == bmodel.productHelper.getProductMasterBOById(schemeProductBO.getProductId()).getOuUomid())
                ((TextView) view.findViewById(R.id.outer_qty)).setText(String.valueOf(schemeProductBO.getQuantitySelected()));
        }

    }

    @Override
    public void updateAmountDetails(String orderVal, String discountAmt, String taxAmt) {

        ((TextView)findViewById(R.id.discount_value_title)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        ((TextView)findViewById(R.id.tax_value_title)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        ((TextView)findViewById(R.id.order_value_title)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));


        TextView discount_value = findViewById(R.id.discount_value);
        TextView taxValue = findViewById(R.id.tax_value);
        TextView orderValue = findViewById(R.id.order_value);

        discount_value.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        taxValue.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        orderValue.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

        discount_value.setTextColor(Color.parseColor("#000000"));
        taxValue.setTextColor(Color.parseColor("#000000"));
        orderValue.setTextColor(Color.parseColor("#000000"));

        discount_value.setText(discountAmt == null || discountAmt.equals("") ?"0":discountAmt);
        taxValue.setText(taxAmt == null || taxAmt.equals("") ?"0":taxAmt);
        orderValue.setText(orderVal == null || orderVal.equals("") ?"0":orderVal);

    }

    @Override
    public void updateSaveStatus(boolean isSuccess) {
        if (isSuccess)
            finish();
    }


    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

        private Vector<ProductMasterBO> productList;
        private CustomKeyBoard dialogCustomKeyBoard;

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

                productName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

                if (!bmodel.configurationMasterHelper.SHOW_ORDER_PCS)
                    pieceQty.setVisibility(View.GONE);
                else {
                    try {
                        pieceQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                        if (bmodel.labelsMasterHelper.applyLabels(pieceQty.getTag()) != null)
                            pieceQty.setText(bmodel.labelsMasterHelper
                                    .applyLabels(pieceQty.getTag()));
                    } catch (Exception e) {
                        Commons.printException(e + "");
                    }
                }

                if (!bmodel.configurationMasterHelper.SHOW_ORDER_CASE)
                    caseQty.setVisibility(View.GONE);
                else {
                    try {
                        caseQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                        if (bmodel.labelsMasterHelper.applyLabels(caseQty.getTag()) != null)
                            caseQty.setText(bmodel.labelsMasterHelper
                                    .applyLabels(caseQty.getTag()));
                    } catch (Exception e) {
                        Commons.printException(e + "");
                    }
                }

                if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE)
                    outerQty.setVisibility(View.GONE);
                else {
                    try {
                        outerQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
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
                        sihQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                        if (bmodel.labelsMasterHelper.applyLabels(sihQty.getTag()) != null)
                            sihQty.setText(bmodel.labelsMasterHelper
                                    .applyLabels(sihQty.getTag()));
                    } catch (Exception e) {
                        Commons.printException(e + "");
                    }
                }

                if (!bmodel.configurationMasterHelper.SHOW_SALES_RETURN_IN_ORDER)
                    srpQty.setVisibility(View.GONE);
                else {
                    try {
                        srpQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                        if (bmodel.labelsMasterHelper.applyLabels(srpQty.getTag()) != null)
                            srpQty.setText(bmodel.labelsMasterHelper
                                    .applyLabels(srpQty.getTag()));
                    } catch (Exception e) {
                        Commons.printException(e + "");
                    }
                }

                if (!bmodel.configurationMasterHelper.SHOW_SALES_RETURN_IN_ORDER)
                    srQty.setVisibility(View.GONE);
                else {
                    try {
                        srQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
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

            holder.pieceQty.setTag(String.valueOf(productList.get(position).getOrderedPcsQty()));
            holder.caseQty.setTag(String.valueOf(productList.get(position).getOrderedCaseQty()*productList.get(position).getCaseSize()));
            holder.outerQty.setTag(String.valueOf(productList.get(position).getOrderedOuterQty()*productList.get(position).getOutersize()));

            holder.sihQty.setText(String.valueOf(productList.get(position).getSIH()));

            if (bmodel.configurationMasterHelper.SHOW_SALES_RETURN_IN_ORDER) {
                int total = 0;
                if (productList.get(position).getSalesReturnReasonList() != null) {
                    for (SalesReturnReasonBO obj : productList.get(position).getSalesReturnReasonList())
                        total = total + obj.getPieceQty() + (obj.getCaseQty() * obj.getCaseSize()) + (obj.getOuterQty() * obj.getOuterSize());
                }
                String strTotal = Integer.toString(total);
                holder.srQty.setText(strTotal);
            }

            int totalReplaceQty =  (productList.get(position).getRepCaseQty() * productList.get(position).getCaseSize())
                                    +productList.get(position).getRepPieceQty()
                                    +(productList.get(position).getRepOuterQty() * productList.get(position).getOutersize());

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

                    if (totalQty <= productList.get(position).getSIH() &&
                            SDUtil.convertToInt(qty) <= storedPieceQty ) {
                        if (!"".equals(qty)) {
                            productList.get(position).setOrderedPcsQty(SDUtil
                                    .convertToInt(qty));
                        }
                        double tot = (productList.get(position).getOrderedCaseQty() * productList.get(position).getCsrp())
                                + (productList.get(position).getOrderedPcsQty() * productList.get(position).getSrp())
                                + (productList.get(position).getOrderedOuterQty() * productList.get(position).getOsrp());

                        productList.get(position).setTotalamount(tot);
                    } else {
                        if (!"0".equals(qty)) {
                            if (totalQty > productList.get(position).getSIH()) {
                                Toast.makeText(
                                        OrderDeliveryDetailActivity.this,
                                        String.format(
                                                getResources().getString(
                                                        R.string.exceed),
                                                productList.get(position).getSIH()),
                                        Toast.LENGTH_SHORT).show();
                            }else if(SDUtil.convertToInt(qty) > storedPieceQty){
                                Toast.makeText(
                                        OrderDeliveryDetailActivity.this,
                                        String.format(
                                                getResources().getString(
                                                        R.string.exceed_ordered_value),
                                                storedPieceQty),
                                        Toast.LENGTH_SHORT).show();
                            }

                            //Delete the last entered number and reset the qty
                            qty = qty.length() > 1 ? qty.substring(0,
                                    qty.length() - 1) : "0";
                            productList.get(position).setOrderedPcsQty(SDUtil
                                    .convertToInt(qty));
                            holder.pieceQty.setText(qty);
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

                    if (totalQty <= productList.get(position).getSIH() &&
                            (SDUtil.convertToInt(qty) * productList.get(position).getCaseSize()) <= storedcaseQty) {
                        if (!"".equals(qty)) {
                            productList.get(position).setOrderedCaseQty(SDUtil
                                    .convertToInt(qty));
                        }

                        double tot = (productList.get(position).getOrderedCaseQty() * productList.get(position).getCsrp())
                                + (productList.get(position).getOrderedPcsQty() * productList.get(position).getSrp())
                                + (productList.get(position).getOrderedOuterQty() * productList.get(position).getOsrp());
                        productList.get(position).setTotalamount(tot);
                    } else {
                        if (!"0".equals(qty)) {
                            if (totalQty > productList.get(position).getSIH()) {
                                Toast.makeText(
                                        OrderDeliveryDetailActivity.this,
                                        String.format(
                                                getResources().getString(
                                                        R.string.exceed),
                                                productList.get(position).getSIH()),
                                        Toast.LENGTH_SHORT).show();
                            }else if((SDUtil.convertToInt(qty) * productList.get(position).getCaseSize()) > storedcaseQty){
                                Toast.makeText(
                                        OrderDeliveryDetailActivity.this,
                                        String.format(
                                                getResources().getString(
                                                        R.string.exceed_ordered_value),
                                                storedcaseQty/productList.get(position).getCaseSize()),
                                        Toast.LENGTH_SHORT).show();
                            }

                            //Delete the last entered number and reset the qty
                            qty = qty.length() > 1 ? qty.substring(0,
                                    qty.length() - 1) : "0";

                            holder.caseQty.setText(qty);
                            productList.get(position).setOrderedCaseQty(SDUtil
                                    .convertToInt(qty));
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

                    if (totalQty <= productList.get(position).getSIH() &&
                            (SDUtil.convertToInt(qty) * productList.get(position).getOutersize()) <= storedouterQty) {
                        if (!"".equals(qty)) {
                            productList.get(position).setOrderedOuterQty(SDUtil
                                    .convertToInt(qty));
                        }

                        double tot = (productList.get(position).getOrderedCaseQty() * productList.get(position).getCsrp())
                                + (productList.get(position).getOrderedPcsQty() * productList.get(position).getSrp())
                                + (productList.get(position).getOrderedOuterQty() * productList.get(position).getOsrp());
                        productList.get(position).setTotalamount(tot);
                    } else {
                        if (!"0".equals(qty)) {
                            if (totalQty > productList.get(position).getSIH()) {
                                Toast.makeText(
                                        OrderDeliveryDetailActivity.this,
                                        String.format(
                                                getResources().getString(
                                                        R.string.exceed),
                                                productList.get(position).getSIH()),
                                        Toast.LENGTH_SHORT).show();
                            }else if((SDUtil.convertToInt(qty) * productList.get(position).getOutersize()) > storedouterQty){
                                Toast.makeText(
                                        OrderDeliveryDetailActivity.this,
                                        String.format(
                                                getResources().getString(
                                                        R.string.exceed_ordered_value),
                                                storedouterQty/productList.get(position).getOutersize()),
                                        Toast.LENGTH_SHORT).show();
                            }

                            qty = qty.length() > 1 ? qty.substring(0,
                                    qty.length() - 1) : "0";

                            productList.get(position).setOrderedOuterQty(SDUtil
                                    .convertToInt(qty));

                            holder.outerQty.setText(qty);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            backButtonClick();
            return true;
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
                Button ed = (Button) findViewById(vw.getId());
                append = ed.getText().toString();
                eff();
                val = SDUtil.convertToInt(append);
            }
        }

    }

}
