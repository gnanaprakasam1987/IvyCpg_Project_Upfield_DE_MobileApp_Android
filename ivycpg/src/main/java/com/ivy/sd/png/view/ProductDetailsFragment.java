package com.ivy.sd.png.view;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;

import java.util.Vector;

/**
 * Created by nagaganesh.n on 4/26/2017.
 */

public class ProductDetailsFragment extends IvyBaseFragment {

    private BusinessModel bmodel;
    private static final String CURRENCT_LABEL = "currency";
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_product_details, container, false);

        setHasOptionsMenu(true);
        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setDisplayShowTitleEnabled(false);
        }
        setScreenTitle("" + bmodel.productHelper.getPdname());

        productRecycView = (RecyclerView) rootView.findViewById(R.id.product_details_recycview);
        productConfigs = bmodel.configurationMasterHelper.getProductDetails();
        if (productConfigs != null) {
            loadProdDetails();
        }

        DisplayMetrics outMetrics = new DisplayMetrics();
        getActivity().getWindow().getWindowManager().getDefaultDisplay()
                .getMetrics(outMetrics);

        if (flag == 1) {
            TextView productTitleTV = (TextView) rootView.findViewById(R.id.product_info_title);
            productTitleTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
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

        ProdDetailRecyclerAdapter adapter = new ProdDetailRecyclerAdapter(getActivity());
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 4);
        productRecycView.setLayoutManager(mLayoutManager);
        productRecycView.setItemAnimator(new DefaultItemAnimator());
        productRecycView.setAdapter(adapter);
    }

    /**
     * Custom Recyclerview adapter for ProductsDetails.
     */
    public class ProdDetailRecyclerAdapter extends RecyclerView.Adapter<ProdDetailRecyclerAdapter.MyViewHolder> {

        private Context context;
        private int flag = 0;

        public class MyViewHolder extends RecyclerView.ViewHolder {

            private TextView menuTV;
            private TextView valueTV;
            private LinearLayout parentLayout, lineLayout;
            private ConfigureBO configureBO;

            public MyViewHolder(View view) {
                super(view);

                menuTV = (TextView) view
                        .findViewById(R.id.tv_menu_name);
                valueTV = (TextView) view
                        .findViewById(R.id.tv_values);
                parentLayout = (LinearLayout) view
                        .findViewById(R.id.parentLayout);
                lineLayout = (LinearLayout) view
                        .findViewById(R.id.line);

            }
        }

        public ProdDetailRecyclerAdapter(Context context) {
            this.context = context;
        }


        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_product_details, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final ProdDetailRecyclerAdapter.MyViewHolder holder, int position) {

            holder.menuTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.valueTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

            holder.configureBO = productConfigs.get(position);
            holder.menuTV.setText(holder.configureBO.getMenuName());
            holder.valueTV.setText(setValue(holder.configureBO, productObj));

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
     * @param configureBO
     * @param productMasterBO
     * @return
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

            }
        }
        return value;
    }

}
