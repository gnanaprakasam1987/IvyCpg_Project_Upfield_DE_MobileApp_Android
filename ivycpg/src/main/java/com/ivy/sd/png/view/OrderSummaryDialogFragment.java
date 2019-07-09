package com.ivy.sd.png.view;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ivy.cpg.view.order.discount.DiscountHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by rajesh.k on 01-09-2015.
 */
public class OrderSummaryDialogFragment extends DialogFragment {
    private LinkedList<ProductMasterBO> mOrderedProductList;
    private BusinessModel bmodel;
    public InputMethodManager inputManager;

    private ListView mListView;
    TextView mTotalQtyTV, mTotalMRPTV, mTotalUCPTV;

    private ArrayList<Integer> mTypeIdList;
    private SparseArray<ArrayList<Integer>> mDiscountIdListByTypeId;

    private HashMap<String, Double> mDiscountValueByTypewithproductbachtid;

    private DiscountHelper discountHelper;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getDialog() != null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
        getDialog().setCancelable(false);
        this.setCancelable(false);
        View rootView = inflater.inflate(R.layout.dialogfragmaent_ordersummary, container,
                false);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        getDialog().setTitle("OrderSummary Dialog");
        // Do something else
        discountHelper = DiscountHelper.getInstance(getActivity());

        return rootView;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        inputManager = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getView().getWindowToken(), 0);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        mListView = (ListView) getView().findViewById(R.id.lv_ordersummary_dialog);
        mTotalQtyTV = (TextView) getView().findViewById(R.id.tv_total_qty);
        mTotalMRPTV = (TextView) getView().findViewById(R.id.tv_totalmrp);
        mTotalUCPTV = (TextView) getView().findViewById(R.id.tv_totalschemeucp);
        Button mCloseBTN = (Button) getView().findViewById(R.id.btn_close);

        ((TextView) getDialog().findViewById(R.id.txt_pname)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) getDialog().findViewById(R.id.txt_qty)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) getDialog().findViewById(R.id.txt_mrp)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) getDialog().findViewById(R.id.tv_title_price_off)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) getDialog().findViewById(R.id.tv_title_disc1)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) getDialog().findViewById(R.id.tv_title_disc2)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) getDialog().findViewById(R.id.tv_title_disc3)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) getDialog().findViewById(R.id.tv_title_disc4)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) getDialog().findViewById(R.id.tv_title_disc5)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) getDialog().findViewById(R.id.tv_title_cash_discount)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) getDialog().findViewById(R.id.tv_title_tax)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) getDialog().findViewById(R.id.txt_line_value)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

        mTotalQtyTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
        mTotalMRPTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
        mTotalUCPTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
        mCloseBTN.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

        mCloseBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        mOrderedProductList = (LinkedList<ProductMasterBO>) getArguments().getSerializable("OrderList");


        mTypeIdList = bmodel.productHelper.getTypeIdList();
        mDiscountIdListByTypeId = bmodel.productHelper.getDiscountIdListByTypeId();


        mDiscountValueByTypewithproductbachtid = new HashMap<String, Double>();
        if (mOrderedProductList != null) {
            hideAndSeek();
            updateList();
            getArguments().remove("OrderList");
        }


    }

    private void updateList() {
        double totalMRP = 0;
        double totalPriceOffValue = 0;
        int totalAllQty = 0;

        List<ProductMasterBO> mBatchwiseOrderList = new ArrayList<>();


        for (ProductMasterBO productBO : mOrderedProductList) {
            if (productBO.getBatchwiseProductCount() > 0) {
                ArrayList<ProductMasterBO> batchList = bmodel.batchAllocationHelper.getBatchlistByProductID().get(productBO.getProductID());
                if (batchList != null) {
                    for (ProductMasterBO batchproductBO : batchList) {
                        int totalQty = batchproductBO.getOrderedPcsQty() + (batchproductBO.getOrderedCaseQty() * productBO.getCaseSize())
                                + (batchproductBO.getOrderedOuterQty() * productBO.getOutersize());
                        if (totalQty > 0) {
                            String productBatchid = batchproductBO.getProductID() + batchproductBO.getBatchid();
                            updateDiscoutByTypeId(productBatchid);
                            mBatchwiseOrderList.add(batchproductBO);
                            totalAllQty = totalAllQty + totalQty;

                            totalMRP = totalMRP + ((batchproductBO.getSrp() + batchproductBO.getPriceoffvalue()) * totalQty);

                            totalPriceOffValue = totalPriceOffValue + (totalQty * batchproductBO.getSrp());

                        }
                    }
                }
            } else {
                updateDiscoutByTypeId(productBO.getProductID());
                mBatchwiseOrderList.add(productBO);

                int totalQty = productBO.getOrderedPcsQty() + (productBO.getOrderedCaseQty() * productBO.getCaseSize())
                        + (productBO.getOrderedOuterQty() * productBO.getOutersize());
                totalAllQty = totalAllQty + totalQty;
                totalMRP = totalMRP + (productBO.getSrp() + productBO.getPriceoffvalue());
                totalPriceOffValue = totalPriceOffValue + productBO.getPriceoffvalue();
            }


        }

        mTotalQtyTV.setText(totalAllQty + "");
        mTotalMRPTV.setText(SDUtil.format(totalMRP, 2, 0));
        mTotalUCPTV.setText(SDUtil.format(totalPriceOffValue, 2, 0));

        mListView.setAdapter(new MyAdapter(mBatchwiseOrderList));


    }


    private void updateDiscoutByTypeId(String prodcutWithBatchid) {
        HashMap<Integer, Double> discountValueByDiscountId = discountHelper.getDiscountListByProductId().get(prodcutWithBatchid);

        if (mTypeIdList != null && discountValueByDiscountId != null) {
            if (mDiscountIdListByTypeId != null) {
                for (Integer typeId : mTypeIdList) {
                    ArrayList<Integer> discountIdList = mDiscountIdListByTypeId.get(typeId);
                    if (discountIdList != null) {
                        double totalDiscountValueByTypeId = 0;
                        for (Integer discountId : discountIdList) {
                            if (discountValueByDiscountId.get(discountId) != null) {
                                double discount = discountValueByDiscountId.get(discountId);
                                totalDiscountValueByTypeId = totalDiscountValueByTypeId + discount;
                            }
                        }
                        mDiscountValueByTypewithproductbachtid.put(typeId + prodcutWithBatchid, totalDiscountValueByTypeId);
                    }
                }
            }
        }

    }

    private void hideAndSeek() {

        if (bmodel.configurationMasterHelper.SHOW_ORD_SUMMARY_PRICEOFF) {
            TextView priceOffTV = (TextView) getView().findViewById(R.id.tv_title_price_off);
            String textPriceOff = bmodel.productHelper.getPriceOffTextname();
            if (textPriceOff != null && !textPriceOff.equals("")) {
                priceOffTV.setText(textPriceOff);
            }
            priceOffTV.setVisibility(View.VISIBLE);
        }
        if (bmodel.configurationMasterHelper.SHOW_ORD_SUMMARY_CASH_DISCOUNT) {
            TextView caseDiscTV = (TextView) getView().findViewById(R.id.tv_title_cash_discount);
            caseDiscTV.setVisibility(View.VISIBLE);
        }
        if (bmodel.configurationMasterHelper.SHOW_ORD_SUMMARY_TAX) {
            TextView taxTV = (TextView) getView().findViewById(R.id.tv_title_tax);
            taxTV.setVisibility(View.VISIBLE);
        }
        TextView tv1 = (TextView) getView().findViewById(R.id.tv_title_disc1);
        TextView tv2 = (TextView) getView().findViewById(R.id.tv_title_disc2);
        TextView tv3 = (TextView) getView().findViewById(R.id.tv_title_disc3);
        TextView tv4 = (TextView) getView().findViewById(R.id.tv_title_disc4);
        TextView tv5 = (TextView) getView().findViewById(R.id.tv_title_disc5);
        if (bmodel.configurationMasterHelper.SHOW_ORD_SUMMARY_DISC1) {

            tv1.setVisibility(View.VISIBLE);
        }
        if (bmodel.configurationMasterHelper.SHOW_ORD_SUMMARY_DISC2) {

            tv2.setVisibility(View.VISIBLE);
        }
        if (bmodel.configurationMasterHelper.SHOW_ORD_SUMMARY_DISC3) {

            tv3.setVisibility(View.VISIBLE);
        }
        if (bmodel.configurationMasterHelper.SHOW_ORD_SUMMARY_DISC4) {

            tv4.setVisibility(View.VISIBLE);
        }
        if (bmodel.configurationMasterHelper.SHOW_ORD_SUMMARY_DISC5) {
            tv5.setVisibility(View.VISIBLE);
        }
        HashMap<Integer, String> descriptionByTypeId = bmodel.productHelper.getDescriptionByTypeId();

        int count = 0;
        if (descriptionByTypeId != null) {
            for (Integer typeId : mTypeIdList) {
                count = count + 1;
                if (descriptionByTypeId.get(typeId) != null) {
                    switch (count) {
                        case 1:
                            tv1.setText(descriptionByTypeId.get(typeId));
                            break;
                        case 2:
                            tv2.setText(descriptionByTypeId.get(typeId));
                            break;
                        case 3:
                            tv3.setText(descriptionByTypeId.get(typeId));
                            break;
                        case 4:
                            tv4.setText(descriptionByTypeId.get(typeId));
                            break;
                        case 5:
                            tv5.setText(descriptionByTypeId.get(typeId));
                            break;
                    }

                }
            }
        }


    }


    class MyAdapter extends ArrayAdapter<ProductMasterBO> {
        private List<ProductMasterBO> items;

        @Override
        public ProductMasterBO getItem(int position) {
            return items.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return items.size();
        }

        public MyAdapter(List<ProductMasterBO> items) {
            super(getActivity(),
                    R.layout.list_dialog_ordersummary, items);
            this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(
                        R.layout.list_dialog_ordersummary, parent,
                        false);
                holder = new ViewHolder();
                holder.productnameTV = (TextView) row.findViewById(R.id.tv_product_name);
                holder.productnameTV.setMaxLines(bmodel.configurationMasterHelper.MAX_NO_OF_PRODUCT_LINES);
                holder.batchCodeTV = (TextView) row.findViewById(R.id.tv_batch_code);
                holder.totalValueTV = (TextView) row.findViewById(R.id.tv_totalvalue);
                holder.priceOffTV = (TextView) row.findViewById(R.id.tv_priceoff_value);
                holder.disc1TV = (TextView) row.findViewById(R.id.tv_disc1);
                holder.disc2TV = (TextView) row.findViewById(R.id.tv_disc2);
                holder.disc3TV = (TextView) row.findViewById(R.id.tv_disc3);
                holder.disc4TV = (TextView) row.findViewById(R.id.tv_disc4);
                holder.disc5TV = (TextView) row.findViewById(R.id.tv_disc5);
                holder.cashDiscountTV = (TextView) row.findViewById(R.id.tv_cash_discount);
                holder.taxValueTV = (TextView) row.findViewById(R.id.tv_tax_value);
                holder.lineValueTV = (TextView) row.findViewById(R.id.tv_line_value);
                holder.totalQtyTV = (TextView) row.findViewById(R.id.tv_total_qty);
                if (bmodel.configurationMasterHelper.SHOW_ORD_SUMMARY_PRICEOFF) {

                    holder.priceOffTV.setVisibility(View.VISIBLE);
                }
                if (bmodel.configurationMasterHelper.SHOW_ORD_SUMMARY_CASH_DISCOUNT) {

                    holder.cashDiscountTV.setVisibility(View.VISIBLE);
                }
                if (bmodel.configurationMasterHelper.SHOW_ORD_SUMMARY_TAX) {

                    holder.taxValueTV.setVisibility(View.VISIBLE);
                }
                if (bmodel.configurationMasterHelper.SHOW_ORD_SUMMARY_DISC1) {

                    holder.disc1TV.setVisibility(View.VISIBLE);
                }
                if (bmodel.configurationMasterHelper.SHOW_ORD_SUMMARY_DISC2) {

                    holder.disc2TV.setVisibility(View.VISIBLE);
                }
                if (bmodel.configurationMasterHelper.SHOW_ORD_SUMMARY_DISC3) {

                    holder.disc3TV.setVisibility(View.VISIBLE);
                }
                if (bmodel.configurationMasterHelper.SHOW_ORD_SUMMARY_DISC4) {

                    holder.disc4TV.setVisibility(View.VISIBLE);
                }
                if (bmodel.configurationMasterHelper.SHOW_ORD_SUMMARY_DISC5) {

                    holder.disc5TV.setVisibility(View.VISIBLE);
                }


                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }
            holder.batchProductBO = items.get(position);
            holder.productnameTV.setText(holder.batchProductBO.getProductShortName());


            holder.totalValueTV.setText(SDUtil.format((holder.batchProductBO.getSrp() + holder.batchProductBO.getPriceoffvalue()), 2, 0));
            ProductMasterBO productBO = bmodel.productHelper.getProductMasterBOById(holder.batchProductBO.getProductID());
            String batchId = "";
            String batchNo = "";
            if (productBO != null) {
                int totalQty = holder.batchProductBO.getOrderedPcsQty()
                        + holder.batchProductBO.getOrderedCaseQty() * productBO.getCaseSize()
                        + holder.batchProductBO.getOrderedOuterQty() * productBO.getOutersize();
                holder.priceOffTV.setText((SDUtil.format(totalQty * holder.batchProductBO.getPriceoffvalue(), 2, 0)));
                if (productBO.getBatchwiseProductCount() > 0) {
                    batchId = holder.batchProductBO.getBatchid();
                    batchNo = holder.batchProductBO.getBatchNo();
                } else {
                    batchId = "";
                    batchNo = "";
                }
                holder.totalQtyTV.setText(totalQty + "");
            } else {
                batchId = "";
                batchNo = "";
            }
            holder.batchCodeTV.setText(batchNo);
            String batchWithpid = holder.batchProductBO.getProductID() + batchId;

            // Apply level discount
            HashMap<Integer, Double> discountValueByDiscountId = discountHelper.getDiscountListByProductId().get(batchWithpid);
            if (discountValueByDiscountId != null) {
                if (mTypeIdList != null) {
                    int count = 0;
                    for (Integer typeId : mTypeIdList) {
                        count = count + 1;

                        if (mDiscountValueByTypewithproductbachtid.get(typeId + batchWithpid) != null) {
                            final double discount = mDiscountValueByTypewithproductbachtid.get(typeId + batchWithpid);
                            switch (count) {
                                case 1:
                                    holder.disc1TV.setText(SDUtil.format(discount, 2, 0));
                                    break;
                                case 2:
                                    holder.disc2TV.setText(SDUtil.format(discount, 2, 0));
                                    break;
                                case 3:
                                    holder.disc3TV.setText(SDUtil.format(discount, 2, 0));
                                    break;
                                case 4:
                                    holder.disc4TV.setText(SDUtil.format(discount, 2, 0));
                                    break;
                                case 5:
                                    holder.disc5TV.setText(SDUtil.format(discount, 2, 0));
                                    break;
                            }

                        }

                    }
                }

                // entry level discount
                if (discountValueByDiscountId.get(0) != null) {
                    double cashdiscount = discountValueByDiscountId.get(0);
                    holder.cashDiscountTV.setText(SDUtil.format(cashdiscount, 2, 0));
                    Commons.print(">?>>>>>>," + cashdiscount + "");

                }
            } else {
                holder.disc1TV.setText("0");
                holder.disc2TV.setText("0");
                holder.disc3TV.setText("0");
                holder.disc4TV.setText("0");
                holder.disc5TV.setText("0");
            }
            holder.taxValueTV.setText(SDUtil.format(holder.batchProductBO.getTaxAmount(), 2, 0));
            holder.lineValueTV.setText(SDUtil.format(holder.batchProductBO.getNetValue(), 2, 0));


            return row;
        }
    }

    class ViewHolder {
        TextView productnameTV, batchCodeTV, totalValueTV, disc1TV, disc2TV, disc3TV, disc4TV, disc5TV;
        TextView priceOffTV, cashDiscountTV, taxValueTV, lineValueTV, totalQtyTV;
        ProductMasterBO batchProductBO;
    }

}
