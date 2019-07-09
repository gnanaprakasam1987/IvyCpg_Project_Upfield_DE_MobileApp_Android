package com.ivy.cpg.view.reports.orderreport;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ivy.lib.Utils;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FileUtils;
import com.ivy.utils.FontUtils;


import java.io.File;
import java.util.ArrayList;

import butterknife.ButterKnife;


public class OrderReportAdapter extends ArrayAdapter<OrderReportBO> {
    ArrayList<OrderReportBO> items;
    private BusinessModel businessModel;
    private Context mContext;
    private IOrderReportImageView iOrderReportImageView;

    public OrderReportAdapter(ArrayList<OrderReportBO> items, Context mContext, BusinessModel businessModel, IOrderReportImageView iOrderReportImageView) {
        super(mContext, R.layout.row_order_report, items);
        this.items = items;
        this.mContext = mContext;
        this.businessModel = businessModel;
        this.iOrderReportImageView = iOrderReportImageView;
    }

    @Override
    @NonNull
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        View row = convertView;

        if (row == null) {

            row = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_order_report, parent, false);
            holder = new ViewHolder(row);
            holder.text_retailerName = row.findViewById(R.id.prd_nameTv);
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

            holder.orderImage = row.findViewById(R.id.ord_img_view);

            holder.tvVolumeValue = row.findViewById(R.id.tv_volume);
            holder.tvVolumeLabel = row.findViewById(R.id.tv_volume_title);

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

            if (!businessModel.configurationMasterHelper.IS_SHOW_ORDER_PHOTO_CAPTURE)
                holder.orderImage.setVisibility(View.GONE);

            if (!businessModel.configurationMasterHelper.SHOW_VOLUME_QTY) {
                holder.tvVolumeLabel.setVisibility(View.GONE);
                holder.tvVolumeValue.setVisibility(View.GONE);
            }





            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        holder.reportBO = items .get(position);

        holder.text_retailerName.setText(holder.reportBO.getRetailerName());
        holder.text_retailerName.setTypeface(FontUtils.getFontRoboto(mContext, FontUtils.FontType.MEDIUM));
        holder.text_orderValue.setTypeface(FontUtils.getFontRoboto(mContext, FontUtils.FontType.MEDIUM));
        holder.label_orderNumber.setTypeface(FontUtils.getFontRoboto(mContext, FontUtils.FontType.LIGHT));
        holder.tvOrderNo.setTypeface(FontUtils.getFontRoboto(mContext, FontUtils.FontType.LIGHT));
        holder.text_delivery_date.setTypeface(FontUtils.getFontRoboto(mContext, FontUtils.FontType.LIGHT));
        holder.tvFocusBrandCount.setTypeface(FontUtils.getFontRoboto(mContext, FontUtils.FontType.LIGHT));
        holder.tvMustSellCount.setTypeface(FontUtils.getFontRoboto(mContext, FontUtils.FontType.LIGHT));
        holder.tvFocusBrandCount.setTypeface(FontUtils.getFontRoboto(mContext, FontUtils.FontType.LIGHT));
        holder.tvVolumeValue.setTypeface(FontUtils.getFontRoboto(mContext, FontUtils.FontType.LIGHT));

        holder.label_LPC.setTypeface(FontUtils.getFontRoboto(mContext, FontUtils.FontType.MEDIUM));
        holder.label_weight.setTypeface(FontUtils.getFontRoboto(mContext, FontUtils.FontType.MEDIUM));
        holder.label_PreORPost.setTypeface(FontUtils.getFontRoboto(mContext, FontUtils.FontType.MEDIUM));
        holder.label_focusBrand.setTypeface(FontUtils.getFontRoboto(mContext, FontUtils.FontType.MEDIUM));
        holder.label_MustSell.setTypeface(FontUtils.getFontRoboto(mContext, FontUtils.FontType.MEDIUM));
        holder.tv_tax_value.setTypeface(FontUtils.getFontRoboto(mContext, FontUtils.FontType.LIGHT));
        holder.tv_discount_amt.setTypeface(FontUtils.getFontRoboto(mContext, FontUtils.FontType.LIGHT));
        holder.taxTitle.setTypeface(FontUtils.getFontRoboto(mContext, FontUtils.FontType.MEDIUM));
        holder.discTitle.setTypeface(FontUtils.getFontRoboto(mContext, FontUtils.FontType.MEDIUM));
        holder.tvVolumeLabel.setTypeface(FontUtils.getFontRoboto(mContext, FontUtils.FontType.MEDIUM));

        try {
            if (businessModel.labelsMasterHelper.applyLabels(holder.tvMustSellCount.getTag()) != null) {
                String value = businessModel.labelsMasterHelper.applyLabels(holder.tvMustSellCount.getTag()) + " : " + holder.reportBO.getMustSellCount();
                holder.tvMustSellCount.setText(value);
            } else {
                String value = mContext.getResources().getString(R.string.must_sell) + " : " + holder.reportBO.getMustSellCount();
                holder.tvMustSellCount.setText(value);
                holder.text_mustSellCount.setText(String.valueOf(holder.reportBO.getMustSellCount()));

            }
            if (businessModel.labelsMasterHelper.applyLabels(holder.tvFocusBrandCount.getTag()) != null) {
                String value = businessModel.labelsMasterHelper.applyLabels(holder.tvFocusBrandCount.getTag()) + " : " + holder.reportBO.getFocusBrandCount();
                holder.tvFocusBrandCount.setText(value);
            } else {
                String value = mContext.getResources().getString(R.string.focus_brand) + " : " + holder.reportBO.getFocusBrandCount();
                holder.tvFocusBrandCount.setText(value);
                holder.focus_brand_count1.setText(String.valueOf(holder.reportBO.getFocusBrandCount()));

            }

            if (!businessModel.configurationMasterHelper.SHOW_STK_ORD_SRP) {
                holder.text_orderValue.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Commons.printException(e);
        }

        holder.text_orderValue.setText(businessModel.formatValue((holder.reportBO
                .getOrderTotal())));


        holder.text_LPC.setText(holder.reportBO.getLPC());
        holder.tvwDist.setText(holder.reportBO.getDist());
        holder.tvOrderNo.setText(holder.reportBO.getOrderID());
        holder.tvWeight.setText(Utils.formatAsTwoDecimal((double)holder.reportBO.getWeight()));


        try {

            StringBuilder sb = new StringBuilder();
            String op = mContext.getResources().getString(R.string.item_piece);
            String oc = mContext.getResources().getString(R.string.item_case);
            String ou = mContext.getResources().getString(R.string.item_outer);

            if (businessModel.labelsMasterHelper
                    .applyLabels("item_piece") != null)
                op = businessModel.labelsMasterHelper
                        .applyLabels("item_piece");
            if (businessModel.labelsMasterHelper
                    .applyLabels("item_case") != null)
                oc = businessModel.labelsMasterHelper
                        .applyLabels("item_case");

            if (businessModel.labelsMasterHelper
                    .applyLabels("item_outer") != null)
                ou = businessModel.labelsMasterHelper
                        .applyLabels("item_outer");


            if (businessModel.configurationMasterHelper.SHOW_ORDER_PCS) {

                sb.append(holder.reportBO.getVolumePcsQty() + " " + op + " ");
            }


            if (businessModel.configurationMasterHelper.SHOW_ORDER_CASE) {

                if (businessModel.configurationMasterHelper.SHOW_ORDER_PCS)
                    sb.append(": " + (holder.reportBO.getVolumeCaseQty()) + " "
                            + oc + " ");
                else
                    sb.append(holder.reportBO.getVolumeCaseQty() + " "
                            + oc + " ");
            }

            if (businessModel.configurationMasterHelper.SHOW_OUTER_CASE) {
                if (businessModel.configurationMasterHelper.SHOW_ORDER_PCS || businessModel.configurationMasterHelper.SHOW_ORDER_CASE)
                    sb.append(": " + (holder.reportBO.getVolumeOuterQty()) + " "
                            + ou + " ");
                else
                    sb.append(holder.reportBO.getVolumeCaseQty() + " "
                            + ou + " ");
            }

            holder.tvVolumeValue.setText(sb.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }


        if (businessModel.configurationMasterHelper.HAS_SELLER_TYPE_SELECTION_ENABLED) {
            if (holder.reportBO.getIsVanSeller() == 1)
                holder.tv_seller_type.setText("V");
            else
                holder.tv_seller_type.setText("P");
        } else {
            holder.tv_seller_type.setVisibility(View.INVISIBLE);
        }

        if (holder.reportBO.getUpload().equalsIgnoreCase("Y")) {
            holder.text_retailerName.setTextColor(mContext.getResources().getColor(
                    R.color.green_productivity));
            holder.text_orderValue.setTextColor(mContext.getResources().getColor(
                    R.color.green_productivity));
            holder.text_LPC.setTextColor(mContext.getResources().getColor(
                    R.color.green_productivity));
            holder.tvwDist.setTextColor(mContext.getResources().getColor(
                    R.color.green_productivity));
            holder.tvOrderNo.setTextColor(mContext.getResources().getColor(
                    R.color.green_productivity));

        } else {

            row.setBackgroundDrawable(mContext.getResources().getDrawable(
                    R.drawable.list_selector));
        }

        if (businessModel.configurationMasterHelper.IS_SHOW_ORDER_PHOTO_CAPTURE) {
            if (holder.reportBO.getUpload().equalsIgnoreCase("Y")
                    && !businessModel.checkForNFilesInFolder(FileUtils.photoFolderPath,
                    1, holder.reportBO.getOrderedImage())) {

                holder.orderImage.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.no_image_available));

            } else {

                Glide.with(mContext)
                        .load(FileUtils.photoFolderPath + "/" + holder.reportBO.getOrderedImage())
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .centerCrop()
                        .transform(businessModel.circleTransform)
                        .placeholder(R.drawable.no_image_available)
                        .into(holder.orderImage);

            }
        }

        holder.orderImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.reportBO.getOrderedImage() != null) {
                    File imgFile = new File(FileUtils.photoFolderPath + "/" + holder.reportBO.getOrderedImage());
                    if (imgFile.exists() && !"".equals(holder.reportBO.getOrderedImage())) {
                        try {
                            iOrderReportImageView.openImageView(imgFile.getAbsolutePath());
                        } catch (Exception e) {
                            Commons.printException("" + e);
                        }
                    } else {
                        Toast.makeText(mContext,
                                mContext.getResources().getString(R.string.unloadimage),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        try {
            String delivery_date;

            delivery_date = DateTimeUtils.convertFromServerDateToRequestedFormat(businessModel.getDeliveryDate(holder.reportBO.getOrderID(),holder.reportBO.getRetailerId()), ConfigurationMasterHelper.outDateFormat);

            if (businessModel.labelsMasterHelper.applyLabels(holder.text_delivery_date.getTag()) != null) {
                String value = businessModel.labelsMasterHelper.applyLabels(holder.text_delivery_date.getTag()) + " : " + delivery_date;
                holder.text_delivery_date.setText(value);
            } else {
                String value = mContext.getResources().getString(R.string.delivery_date_label) + " : " + delivery_date;
                holder.text_delivery_date.setText(value);
            }

            if (businessModel.labelsMasterHelper.applyLabels(holder.taxTitle.getTag()) != null)
                holder.taxTitle.setText(businessModel.labelsMasterHelper.applyLabels(holder.taxTitle.getTag()));

            holder.tv_tax_value.setText(businessModel.formatValue(holder.reportBO.getTaxValue()));


            if (businessModel.labelsMasterHelper.applyLabels(holder.discTitle.getTag()) != null)
                holder.discTitle.setText(businessModel.labelsMasterHelper.applyLabels(holder.discTitle.getTag()));

            holder.tv_discount_amt.setText(businessModel.formatValue(holder.reportBO.getDiscountValue()));

        } catch (Exception e) {
            Commons.printException(e);
        }


        return (row);
    }

    class ViewHolder {
        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
        OrderReportBO reportBO;
        TextView text_retailerName, label_orderNumber;
        TextView text_orderValue, text_LPC, tvwDist, tvWeight, label_LPC, label_PreORPost, focus_brand_count1, text_mustSellCount, tvVolumeValue;
        TextView text_delivery_date, tv_tax_value, tv_discount_amt, taxTitle, discTitle;
        TextView tvOrderNo, tvFocusBrandCount, tvMustSellCount, tv_seller_type, label_weight,
                label_focusBrand, label_MustSell, focusbrandlabel, mustselllabel, tvVolumeLabel;
        ImageView orderImage;

    }
}



