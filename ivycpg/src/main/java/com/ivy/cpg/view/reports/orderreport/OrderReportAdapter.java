package com.ivy.cpg.view.reports.orderreport;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DateUtil;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import butterknife.ButterKnife;


public class OrderReportAdapter extends ArrayAdapter<OrderReportBO> {
    ArrayList<OrderReportBO> items;
    private BusinessModel businessModel;
    private Context mContext;

    public OrderReportAdapter(ArrayList<OrderReportBO> items, Context mContext, BusinessModel businessModel) {
        super(mContext, R.layout.row_order_report, items);
        this.items = items;
        this.mContext = mContext;
       this. businessModel = businessModel;
    }

    @Override
    @NonNull
    public View getView(int position, View convertView, @NotNull ViewGroup parent) {
        final ViewHolder holder;

        OrderReportBO reportBO = items
                .get(position);
        View row = convertView;

        if (row == null) {

            row = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_order_report, parent, false);
            holder = new ViewHolder(row);
            holder.text_retailerName = row.findViewById(R.id.PRDNAME);
            holder.label_orderNumber = row.findViewById(R.id.ordertxt);
            holder.text_delivery_date = row.findViewById(R.id.text_delivery_date);
            holder.tvFocusBrandCount = row.findViewById(R.id.focus_brand_count);
            holder.tvMustSellCount = row.findViewById(R.id.mustsell_count);

            holder.text_orderValue = row.findViewById(R.id.PRDMRP);
            holder.text_LPC = row.findViewById(R.id.PRDRP);
            holder.tvwDist = row.findViewById(R.id.dist_txt);
            holder.tvOrderNo = row.findViewById(R.id.orderno);
            holder.tvWeight = row.findViewById(R.id.tv_weight);
            holder.label_weight = row.findViewById(R.id.weighttitle);
            holder.tv_seller_type = row.findViewById(R.id.tv_seller_type);
            holder.label_LPC = row.findViewById(R.id.lpc);
            holder.label_PreORPost = row.findViewById(R.id.outid);
            holder.label_focusBrand = row.findViewById(R.id.focusbrand_label);
            holder.label_MustSell = row.findViewById(R.id.mustsell_label);
            holder.focus_brand_count1 = row.findViewById(R.id.focus_brand_count1);
            holder.text_mustSellCount = row.findViewById(R.id.mustsellcount);
            (row.findViewById(R.id.invoiceview_doted_line)).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            holder.focusbrandlabel = row.findViewById(R.id.focusbrand_label);
            holder.mustselllabel = row.findViewById(R.id.mustsell_label);

            holder.tv_tax_value = row.findViewById(R.id.tv_tax_amount);
            holder.tv_discount_amt = row.findViewById(R.id.tv_disc_amt);
            holder.taxTitle = row.findViewById(R.id.tv_tax_title);
            holder.discTitle = row.findViewById(R.id.tv_disc_title);

            if (!businessModel.configurationMasterHelper.SHOW_ORDER_WEIGHT) {
                holder.tvWeight.setVisibility(View.GONE);
                holder.label_weight.setVisibility(View.GONE);
            }
            if (!businessModel.configurationMasterHelper.IS_FOCUSBRAND_COUNT_IN_REPORT) {
                holder.tvFocusBrandCount.setVisibility(View.GONE);
                holder.focusbrandlabel.setVisibility(View.GONE);
                holder.focus_brand_count1.setVisibility(View.GONE);
            }

            if (!businessModel.configurationMasterHelper.IS_MUSTSELL_COUNT_IN_REPORT) {
                holder.text_mustSellCount.setVisibility(View.GONE);
                holder.mustselllabel.setVisibility(View.GONE);
            }

            if (!businessModel.configurationMasterHelper.IS_DIST_PRE_POST_ORDER) {
                holder.label_PreORPost.setVisibility(View.GONE);
                holder.tvwDist.setVisibility(View.GONE);
            }


            if (!businessModel.configurationMasterHelper.SHOW_DELIVERY_DATE_IN_ORDER_RPT)
                holder.text_delivery_date.setVisibility(View.GONE);

            if (!businessModel.configurationMasterHelper.IS_SHOW_TAX_IN_REPORT) {
                holder.tv_tax_value.setVisibility(View.GONE);
                holder.taxTitle.setVisibility(View.GONE);
            }

            if (!businessModel.configurationMasterHelper.IS_SHOW_DISCOUNT_IN_REPORT) {
                holder.tv_discount_amt.setVisibility(View.GONE);
                holder.discTitle.setVisibility(View.GONE);
            }


            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }


        holder.text_retailerName.setText(reportBO.getRetailerName());
        holder.text_retailerName.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        holder.text_orderValue.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        holder.label_orderNumber.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        holder.tvOrderNo.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        holder.text_delivery_date.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        holder.tvFocusBrandCount.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        holder.tvMustSellCount.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        holder.tvFocusBrandCount.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        holder.label_LPC.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        holder.label_weight.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        holder.label_PreORPost.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        holder.label_focusBrand.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        holder.label_MustSell.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        holder.tv_tax_value.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        holder.tv_discount_amt.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        holder.taxTitle.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        holder.discTitle.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));


        try {
            if (businessModel.labelsMasterHelper.applyLabels(holder.tvMustSellCount.getTag()) != null) {
                String value = businessModel.labelsMasterHelper.applyLabels(holder.tvMustSellCount.getTag()) + " : " + reportBO.getMustSellCount();
                holder.tvMustSellCount.setText(value);
            } else {
                String value = mContext.getResources().getString(R.string.must_sell) + " : " + reportBO.getMustSellCount();
                holder.tvMustSellCount.setText(value);
                holder.text_mustSellCount.setText(String.valueOf(reportBO.getMustSellCount()));

            }
            if (businessModel.labelsMasterHelper.applyLabels(holder.tvFocusBrandCount.getTag()) != null) {
                String value = businessModel.labelsMasterHelper.applyLabels(holder.tvFocusBrandCount.getTag()) + " : " + reportBO.getFocusBrandCount();
                holder.tvFocusBrandCount.setText(value);
            } else {
                String value = mContext.getResources().getString(R.string.focus_brand) + " : " + reportBO.getFocusBrandCount();
                holder.tvFocusBrandCount.setText(value);
                holder.focus_brand_count1.setText(String.valueOf(reportBO.getFocusBrandCount()));

            }

            if (!businessModel.configurationMasterHelper.SHOW_STK_ORD_SRP) {
                holder.text_orderValue.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Commons.printException(e);
        }

        holder.text_orderValue.setText(businessModel.formatValue((reportBO
                .getOrderTotal())));
        holder.text_LPC.setText(reportBO.getLPC());
        holder.tvwDist.setText(reportBO.getDist());
        holder.tvOrderNo.setText(reportBO.getOrderID());
        holder.tvWeight.setText(String.valueOf(reportBO.getWeight()));


        if (businessModel.configurationMasterHelper.IS_SHOW_SELLER_DIALOG) {
            if (reportBO.getIsVanSeller() == 1)
                holder.tv_seller_type.setText("V");
            else
                holder.tv_seller_type.setText("P");
        } else {
            holder.tv_seller_type.setVisibility(View.INVISIBLE);
        }

        if (reportBO.getUpload().equalsIgnoreCase("Y")) {
            holder.text_retailerName.setTextColor(mContext.getResources().getColor(
                    R.color.GREEN));
            holder.text_orderValue.setTextColor(mContext.getResources().getColor(
                    R.color.GREEN));
            holder.text_LPC.setTextColor(mContext.getResources().getColor(
                    R.color.GREEN));
            holder.tvwDist.setTextColor(mContext.getResources().getColor(
                    R.color.GREEN));
            holder.tvOrderNo.setTextColor(mContext.getResources().getColor(
                    R.color.GREEN));

        } else {

            row.setBackgroundDrawable(mContext.getResources().getDrawable(
                    R.drawable.list_selector));
        }

        try {
            String delivery_date;

            delivery_date = DateUtil.convertFromServerDateToRequestedFormat(businessModel.getDeliveryDate(reportBO.getRetailerId()), ConfigurationMasterHelper.outDateFormat);

            if (businessModel.labelsMasterHelper.applyLabels(holder.text_delivery_date.getTag()) != null) {
                String value = businessModel.labelsMasterHelper.applyLabels(holder.text_delivery_date.getTag()) + " : " + delivery_date;
                holder.text_delivery_date.setText(value);
            } else {
                String value = mContext.getResources().getString(R.string.delivery_date_label) + " : " + delivery_date;
                holder.text_delivery_date.setText(value);
            }

            if (businessModel.labelsMasterHelper.applyLabels(holder.taxTitle.getTag()) != null)
                holder.taxTitle.setText(businessModel.labelsMasterHelper.applyLabels(holder.taxTitle.getTag()));

            holder.tv_tax_value.setText(businessModel.formatValue(reportBO.getTaxValue()));


            if (businessModel.labelsMasterHelper.applyLabels(holder.discTitle.getTag()) != null)
                holder.discTitle.setText(businessModel.labelsMasterHelper.applyLabels(holder.discTitle.getTag()));

            holder.tv_discount_amt.setText(businessModel.formatValue(reportBO.getDiscountValue()));

        } catch (Exception e) {
            Commons.printException(e);
        }


        return (row);
    }

    class ViewHolder {
        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }

        TextView text_retailerName, label_orderNumber;
        TextView text_orderValue, text_LPC, tvwDist, tvWeight, label_LPC, label_PreORPost, focus_brand_count1, text_mustSellCount;
        TextView text_delivery_date, tv_tax_value, tv_discount_amt, taxTitle, discTitle;
        TextView tvOrderNo, tvFocusBrandCount, tvMustSellCount, tv_seller_type, label_weight, label_focusBrand, label_MustSell, focusbrandlabel, mustselllabel;

    }
}



