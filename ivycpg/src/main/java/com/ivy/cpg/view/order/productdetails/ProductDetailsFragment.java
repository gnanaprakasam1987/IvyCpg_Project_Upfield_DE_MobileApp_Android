package com.ivy.cpg.view.order.productdetails;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.AppUtils;
import com.ivy.utils.FontUtils;
import com.ivy.utils.StringUtils;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;
import java.util.Vector;

import static android.content.res.Configuration.SCREENLAYOUT_SIZE_LARGE;

/**
 * Created by nagaganesh.n on 4/26/2017
 */

public class ProductDetailsFragment extends IvyBaseFragment {

    private BusinessModel bmodel;
    private String pdname;
    private ProductMasterBO productObj;
    private int flag;
    private View rootView;
    private RecyclerView productRecycView;
    private Vector<ConfigureBO> productConfigs = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        this.productObj = bmodel.productHelper.getProductObj();
        this.flag = bmodel.productHelper.getFlag();
        this.pdname = bmodel.productHelper.getPdname();

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_product_details, container, false);

        setHasOptionsMenu(true);
        if (getActivity() != null && getActivity().getActionBar() != null) {
            getActivity().getActionBar().setDisplayShowTitleEnabled(false);
        }
        setScreenTitle("" + bmodel.productHelper.getPdname());

        productRecycView = rootView.findViewById(R.id.product_details_recycview);
        productConfigs = bmodel.configurationMasterHelper.getProductDetails();
        rootView.findViewById(R.id.ll_sao_view).setVisibility(View.GONE);
        if (productConfigs != null) {
            loadProdDetails();
        }

        DisplayMetrics outMetrics = new DisplayMetrics();
        getActivity().getWindow().getWindowManager().getDefaultDisplay()
                .getMetrics(outMetrics);

        if (flag == 1) {
            TextView productTitleTV = rootView.findViewById(R.id.product_info_title);
            productTitleTV.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
            productTitleTV.setText(pdname);
            productTitleTV.setWidth(outMetrics.widthPixels);
        }


        return rootView;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    /**
     * Method used to call load product configs RecyclerView.
     */
    private void loadProdDetails() {

        // For 7" tablet
        boolean is7InchTablet = this.getResources().getConfiguration()
                .isLayoutSizeAtLeast(SCREENLAYOUT_SIZE_LARGE);

        RecyclerView.LayoutManager mLayoutManager = null;
        ProdDetailRecyclerAdapter adapter = new ProdDetailRecyclerAdapter();
        if (!is7InchTablet) {
            mLayoutManager = new GridLayoutManager(getActivity(), 3);
        } else {
            mLayoutManager = new GridLayoutManager(getActivity(), 4);
        }

        productRecycView.setLayoutManager(mLayoutManager);
        productRecycView.setItemAnimator(new DefaultItemAnimator());
        productRecycView.setAdapter(adapter);
    }

    /**
     * Custom Recyclerview adapter for ProductsDetails.
     */
    public class ProdDetailRecyclerAdapter extends RecyclerView.Adapter<ProdDetailRecyclerAdapter.MyViewHolder> {

        private int flag = 0;

        public class MyViewHolder extends RecyclerView.ViewHolder {

            private TextView menuTV;
            private TextView valueTV;
            private ImageView valueImg;
            private LinearLayout parentLayout;
            private ConfigureBO configureBO;

            public MyViewHolder(View view) {
                super(view);

                menuTV = view
                        .findViewById(R.id.tv_menu_name);
                valueTV = view
                        .findViewById(R.id.tv_values);
                valueImg = view.findViewById(R.id.img_values);
                parentLayout = view
                        .findViewById(R.id.parentLayout);

            }
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_product_details, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull final ProdDetailRecyclerAdapter.MyViewHolder holder, int position) {

            holder.menuTV.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
            holder.valueTV.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));

            holder.configureBO = productConfigs.get(position);

            holder.menuTV.setText(holder.configureBO.getMenuName());
            if(!StringUtils.isEmptyString(setValue(holder.configureBO, productObj))){
                holder.valueTV.setText(setValue(holder.configureBO, productObj));
            }
            if (holder.configureBO.getConfigCode().equalsIgnoreCase("PRODET08")){
                holder.valueTV.setVisibility(View.GONE);
                holder.valueImg.setVisibility(View.VISIBLE);
                // barcode image
                Bitmap bitmap = null;
                try {
                    bitmap = AppUtils.encodeAsBitmap(setValue(holder.configureBO, productObj), BarcodeFormat.CODE_128, 600, 300);
                    holder.valueImg.setImageBitmap(bitmap);
                } catch (WriterException e) {
                    e.printStackTrace();
                }
            } else {
                holder.valueTV.setVisibility(View.VISIBLE);
                holder.valueImg.setVisibility(View.GONE);
            }

            if (holder.configureBO.getConfigCode().equalsIgnoreCase("PRODET14"))
                showSkuMixtureView();

            if (position % 4 < 4) {
                if (position % 4 == 0) {
                    if (flag == 0) {
                        flag = 1;
                    } else {
                        flag = 0;
                    }
                }
            }

            if (flag == 0) {
                holder.parentLayout.setBackgroundColor(getResources().getColor(R.color.list_even_item_bg));
            } else {
                holder.parentLayout.setBackgroundColor(getResources().getColor(R.color.list_odd_item_bg));
            }

        }

        @Override
        public int getItemCount() {
            return productConfigs.size();
        }

    }

    /**
     * Method used to setValue for configs.
     *
     * @param configureBO To get configcode
     * @param productMasterBO To get value
     * @return value for the configcode
     */
    private String setValue(ConfigureBO configureBO, ProductMasterBO productMasterBO) {

        String value = "";
        if (productMasterBO != null) {

            switch (configureBO.getConfigCode()) {

                case "PRODET01": {
                    value = "" + productMasterBO.getCaseSize();
                    break;
                }
                case "PRODET02": {
                    value = "" + productMasterBO.getOutersize();
                    break;
                }
                case "PRODET03": {
                    value = "" + bmodel.formatValue(productMasterBO.getSrp());
                    break;
                }
                case "PRODET04": {
                    value = "" + productMasterBO.getRetailerWiseProductWiseP4Qty();
                    break;
                }
                case "PRODET05": {
                    value = "" + productMasterBO.getRetailerWiseP4StockQty();
                    break;
                }
                case "PRODET06": {
                    value = "" + productMasterBO.getSIH();
                    break;
                }
                case "PRODET07": {
                    value = "" + productMasterBO.getWSIH();
                    break;
                }
                case "PRODET08": {
                    value = "" + productMasterBO.getBarCode();
                    break;
                }
                case "PRODET09": {
                    value = "" + productMasterBO.getOutersize();
                    break;
                }
                case "PRODET10": {
                    value = "" + productMasterBO.getCsrp();
                    break;
                }
                case "PRODET11": {
                    value = "" + productMasterBO.getOsrp();
                    break;
                }
                case "PRODET12": {
                    value = "" + productMasterBO.getBaseprice();
                    break;
                }
                case "PRODET13": {
                    value = "" + productMasterBO.getDescription();
                    break;
                }

                case "PRODET15": {
                    value = "" + productMasterBO.getMarginPrice();
                    break;
                }
                case "PRODET16": {
                    value = "" + productMasterBO.getASRP();
                    break;
                }
            }
        }
        return value;
    }

    //IF configBo has value PRODE14 this config is enabled show SkuMixture Product name
    private void showSkuMixtureView() {
        if (bmodel.productHelper.getSkuMixtureProductName(productObj.getProductID()) != null) {
            ArrayList<String> value = bmodel.productHelper.getSkuMixtureProductName(productObj.getProductID());
            rootView.findViewById(R.id.ll_sao_view).setVisibility(View.VISIBLE);
            TextView soaMixtureTitle = rootView.findViewById(R.id.sku_mixture_title);
            soaMixtureTitle.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));

            try {
                if (bmodel.labelsMasterHelper.applyLabels(rootView.findViewById(
                        R.id.sku_mixture_title).getTag()) != null)
                    ((TextView) rootView.findViewById(R.id.sku_mixture_title))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(rootView.findViewById(
                                            R.id.sku_mixture_title)
                                            .getTag()));
            } catch (Exception e) {
                Commons.printException(e);
            }

            LinearLayout skuMixtureProductName = rootView.findViewById(R.id.ll_sku_mixture_product_name);
            for (int i = 0; i < value.size(); i++) {
                TextView tv = new TextView(getActivity());
                tv.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));
                tv.setText(value.get(i));
                tv.setId(i);
                skuMixtureProductName.addView(tv);
            }
        }
    }


}