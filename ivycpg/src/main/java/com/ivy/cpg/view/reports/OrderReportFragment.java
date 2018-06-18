package com.ivy.cpg.view.reports;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.cpg.view.salesreturn.SalesReturnHelper;
import com.ivy.sd.png.asean.view.BuildConfig;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.JExcelHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DateUtil;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * Order report main screen
 */
public class OrderReportFragment extends IvyBaseFragment implements OnClickListener,
        OnItemClickListener {

    private TextView text_totalOrderValue, text_averagePreOrPost;
    private ListView listView;
    private Button xlsExport;

    private BusinessModel businessModel;

    private ArrayList<OrderReportBO> list;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


        View view = inflater.inflate(R.layout.fragment_order_report, container, false);
        businessModel = (BusinessModel) getActivity().getApplicationContext();
        businessModel.setContext(getActivity());

        if (businessModel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }


        text_totalOrderValue = (TextView) view.findViewById(R.id.txttotal);
        TextView averageLines = (TextView) view.findViewById(R.id.txtavglines);
        text_averagePreOrPost = (TextView) view.findViewById(R.id.txt_dist_pre_post);
        TextView text_LPC = (TextView) view.findViewById(R.id.lpc);
        TextView totalLines = (TextView) view.findViewById(R.id.txttotallines);
        TextView tv_lbl_total_lines = (TextView) view.findViewById(R.id.lbl_total_lines);

        xlsExport = (Button) view.findViewById(R.id.btn_export);
        if (businessModel.configurationMasterHelper.IS_EXPORT_ORDER_REPORT) {
            xlsExport.setVisibility(View.VISIBLE);
        }

        if (businessModel.configurationMasterHelper.IS_ORDER_REPORT_EXPORT_AND_EMAIL) {
            xlsExport.setText(getResources().getString(R.string.export_and_email));
        } else if (businessModel.configurationMasterHelper.IS_ORDER_REPORT_EXPORT_AND_SHARE) {
            xlsExport.setText(getResources().getString(R.string.export_and_share));
        } else {
            xlsExport.setText(getResources().getString(R.string.export));
        }
        listView = (ListView) view.findViewById(R.id.list);
        listView.setCacheColorHint(0);
        xlsExport.setOnClickListener(this);

        listView.setOnItemClickListener(this);
        TextView text_totalValueTitle = (TextView) view.findViewById(R.id.totalvaluetitle);
        TextView lab_dist_pre_post = (TextView) view.findViewById(R.id.lab_dist_pre_post);
        text_LPC.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        text_totalValueTitle.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        lab_dist_pre_post.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        list = businessModel.reportHelper.downloadOrderreport();
        updateOrderGrid();
        int mLPC = businessModel.reportHelper
                .getavglinesfororderbooking("OrderHeader");
        if (businessModel.configurationMasterHelper.SHOW_LPC_ORDER) {

            double mTotalOutlets = businessModel.reportHelper
                    .getorderbookingCount("OrderHeader");
            double result = mLPC / mTotalOutlets;
            String resultS = result + "";
            if (resultS.equals(getResources().getString(R.string.nan))) {
                averageLines.setText(String.valueOf(0));
            } else {
                averageLines.setText(SDUtil.roundIt(result, 2));
            }

        }
        if (businessModel.configurationMasterHelper.SHOW_TOTAL_LINES)
            totalLines.setText(String.valueOf(mLPC));
        if (businessModel.configurationMasterHelper.SHOW_TOTAL_LINES) {
            if (businessModel.configurationMasterHelper.SHOW_TOTAL_QTY_IN_ORDER_REPORT) {
                int totalQty = 0;
                for (OrderReportBO bo : list)
                    totalQty = totalQty + businessModel.reportHelper.getTotalQtyfororder(bo.getOrderID());
                totalLines.setText(String.valueOf(totalQty));
                tv_lbl_total_lines.setText(getResources().getString(R.string.tot_qty));
            } else {
                totalLines.setText(String.valueOf(mLPC));
                tv_lbl_total_lines.setText(getResources().getString(R.string.tot_line));
            }

        }

        if (businessModel.configurationMasterHelper.SHOW_LPC_ORDER) {
            view.findViewById(R.id.lbl_avg_lines).setVisibility(View.VISIBLE);
            averageLines.setVisibility(View.VISIBLE);
        }
        if (businessModel.configurationMasterHelper.SHOW_TOTAL_LINES) {
            totalLines.setVisibility(View.VISIBLE);
            view.findViewById(R.id.lbl_total_lines).setVisibility(View.VISIBLE);
            view.findViewById(R.id.view00).setVisibility(View.VISIBLE);

        }

        if (!businessModel.configurationMasterHelper.IS_DIST_PRE_POST_ORDER) {
            view.findViewById(R.id.view1).setVisibility(View.GONE);
            view.findViewById(R.id.lab_dist_pre_post).setVisibility(View.GONE);
            view.findViewById(R.id.txt_dist_pre_post).setVisibility(View.GONE);
            view.findViewById(R.id.dist).setVisibility(View.GONE);
            ((TextView) view.findViewById(R.id.outna)).setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));


        }
        if (!businessModel.configurationMasterHelper.SHOW_ORDER_WEIGHT)
            view.findViewById(R.id.weighttitle).setVisibility(View.GONE);
        else {
            try {
                if (businessModel.labelsMasterHelper.applyLabels(getActivity().findViewById(
                        R.id.weighttitle).getTag()) != null)
                    ((TextView) getActivity().findViewById(R.id.weighttitle))
                            .setText(businessModel.labelsMasterHelper
                                    .applyLabels(getActivity().findViewById(R.id.weighttitle)
                                            .getTag()));
                ((TextView) view.findViewById(R.id.weighttitle)).setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            } catch (Exception e) {
                Commons.printException(e);
            }
        }
        try {
            if (businessModel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.weighttitle).getTag()) != null)
                ((TextView) view.findViewById(R.id.weighttitle))
                        .setText(businessModel.labelsMasterHelper.applyLabels(view
                                .findViewById(R.id.weighttitle).getTag()));
        } catch (Exception e) {
            Commons.printException(e);
        }


        try {
            if (businessModel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.outna).getTag()) != null)
                ((TextView) view.findViewById(R.id.outna))
                        .setText(businessModel.labelsMasterHelper.applyLabels(view.findViewById(R.id.outna).getTag()));
            ((TextView) view.findViewById(R.id.outna)).setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

        } catch (Exception e) {
            Commons.printException(e);
        }
        try {
            if (businessModel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.lpc).getTag()) != null)
                ((TextView) view.findViewById(R.id.lpc))
                        .setText(businessModel.labelsMasterHelper.applyLabels(view
                                .findViewById(R.id.lpc).getTag()));
            ((TextView) view.findViewById(R.id.lpc)).setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

        } catch (Exception e) {
            Commons.printException(e);
        }
        try {
            if (businessModel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.outid).getTag()) != null)
                ((TextView) view.findViewById(R.id.outid))
                        .setText(businessModel.labelsMasterHelper
                                .applyLabels(view.findViewById(
                                        R.id.outid)
                                        .getTag()));
            ((TextView) view.findViewById(R.id.outid)).setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

        } catch (Exception e) {
            Commons.printException(e);
        }

        try {
            if (businessModel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.lab_total_value).getTag()) != null)
                ((TextView) view.findViewById(R.id.lab_total_value))
                        .setText(businessModel.labelsMasterHelper
                                .applyLabels(view.findViewById(
                                        R.id.lab_total_value)
                                        .getTag()));
        } catch (Exception e) {
            Commons.printException(e);
        }

        if (!businessModel.configurationMasterHelper.SHOW_TOTAL_VALUE_ORDER) {
            text_totalOrderValue.setVisibility(View.GONE);
            text_totalValueTitle.setVisibility(View.GONE);
            view.findViewById(R.id.view0).setVisibility(View.GONE);
        }

        return view;

    }


    public void onClick(View comp) {
        Button vw = (Button) comp;
        if (vw == xlsExport) {
            new XlsExport().execute();
        }

    }

    private void updateOrderGrid() {
        double mTotalValue = 0;
        int pre = 0, post = 0;

        // Show alert if error loading data.
        if (list == null) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.unable_to_load_data),
                    Toast.LENGTH_SHORT).show();
            xlsExport.setVisibility(View.GONE);
            return;
        }
        // Show alert if no order exist.
        if (list.size() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.no_orders_available),
                    Toast.LENGTH_SHORT).show();
            xlsExport.setVisibility(View.GONE);
            return;
        }

        // Calculate the total order value.
        for (OrderReportBO ret : list) {
            mTotalValue = mTotalValue + SDUtil.convertToDouble(SDUtil.format(ret.getOrderTotal(),
                    businessModel.configurationMasterHelper.VALUE_PRECISION_COUNT,
                    0, businessModel.configurationMasterHelper.IS_DOT_FOR_GROUP));
        }

        if (businessModel.configurationMasterHelper.IS_DIST_PRE_POST_ORDER) {
            // Calculate the total order value.
            for (OrderReportBO ret : list) {
                try {
                    String str[] = ret.getDist().split("/");
                    pre = pre + SDUtil.convertToInt(str[0]);
                    post = post + SDUtil.convertToInt(str[1]);
                } catch (Exception e) {
                    Commons.printException(e);
                }

            }
            float mPreAverage = 0, mPostAverage = 0;
            if (list.size() > 0) {
                if (pre > 0) {
                    mPreAverage = (float) pre / (float) list.size();
                }
                if (post > 0) {
                    mPostAverage = (float) post / (float) list.size();
                }

                String value = SDUtil.format(mPreAverage, 1, 0) + "/"
                        + SDUtil.format(mPostAverage, 1, 0);
                text_averagePreOrPost.setText(value);

            } else {
                text_averagePreOrPost.setText("0/0");
            }

        }
        // Format and set on the label
        if (!businessModel.configurationMasterHelper.SHOW_NETAMOUNT_IN_REPORT)
            text_totalOrderValue.setText(SDUtil.format(mTotalValue,
                    businessModel.configurationMasterHelper.VALUE_PRECISION_COUNT,
                    businessModel.configurationMasterHelper.VALUE_COMMA_COUNT, businessModel.configurationMasterHelper.IS_DOT_FOR_GROUP));
        else
            text_totalOrderValue.setText(SDUtil.format(businessModel.reportHelper.getTotValues(getActivity().getApplicationContext()) - SalesReturnHelper.getInstance(getActivity()).getTotalSalesReturnValue(getActivity().getApplicationContext()),
                    businessModel.configurationMasterHelper.VALUE_PRECISION_COUNT,
                    businessModel.configurationMasterHelper.VALUE_COMMA_COUNT, businessModel.configurationMasterHelper.IS_DOT_FOR_GROUP));

        // Load ListView
        MyAdapter mSchedule = new MyAdapter(list);
        listView.setAdapter(mSchedule);

    }

    class MyAdapter extends ArrayAdapter<OrderReportBO> {
        ArrayList<OrderReportBO> items;

        private MyAdapter(ArrayList<OrderReportBO> items) {
            super(getActivity(), R.layout.row_order_report, items);
            this.items = items;
        }

        @Override
        @NonNull
        public View getView(int position, View convertView, @NotNull ViewGroup parent) {
            final ViewHolder holder;

            OrderReportBO reportBO = items
                    .get(position);
            View row = convertView;

            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater
                        .inflate(R.layout.row_order_report, parent, false);
                holder = new ViewHolder();
                holder.text_retailerName = (TextView) row.findViewById(R.id.PRDNAME);
                holder.label_orderNumber = (TextView) row.findViewById(R.id.ordertxt);
                holder.text_delivery_date = (TextView) row.findViewById(R.id.text_delivery_date);
                holder.tvFocusBrandCount = (TextView) row.findViewById(R.id.focus_brand_count);
                holder.tvMustSellCount = (TextView) row.findViewById(R.id.mustsell_count);

                holder.text_orderValue = (TextView) row.findViewById(R.id.PRDMRP);
                holder.text_LPC = (TextView) row.findViewById(R.id.PRDRP);
                holder.tvwDist = (TextView) row.findViewById(R.id.dist_txt);
                holder.tvOrderNo = (TextView) row.findViewById(R.id.orderno);
                holder.tvWeight = (TextView) row.findViewById(R.id.tv_weight);
                holder.label_weight = (TextView) row.findViewById(R.id.weighttitle);
                holder.tv_seller_type = (TextView) row.findViewById(R.id.tv_seller_type);
                holder.label_LPC = (TextView) row.findViewById(R.id.lpc);
                holder.label_PreORPost = (TextView) row.findViewById(R.id.outid);
                holder.label_focusBrand = (TextView) row.findViewById(R.id.focusbrand_label);
                holder.label_MustSell = (TextView) row.findViewById(R.id.mustsell_label);
                holder.focus_brand_count1 = (TextView) row.findViewById(R.id.focus_brand_count1);
                holder.text_mustSellCount = (TextView) row.findViewById(R.id.mustsellcount);
                (row.findViewById(R.id.invoiceview_doted_line)).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                holder.focusbrandlabel = (TextView) row.findViewById(R.id.focusbrand_label);
                holder.mustselllabel = (TextView) row.findViewById(R.id.mustsell_label);
                holder.tvVolumeValue=(TextView) row.findViewById(R.id.tv_volume);
                holder.tvVolumeLabel=(TextView) row.findViewById(R.id.tv_volume_title);

                holder.tv_tax_value = (TextView) row.findViewById(R.id.tv_tax_amount);
                holder.tv_discount_amt = (TextView) row.findViewById(R.id.tv_disc_amt);
                holder.taxTitle = (TextView) row.findViewById(R.id.tv_tax_title);
                holder.discTitle = (TextView) row.findViewById(R.id.tv_disc_title);

                if (!businessModel.configurationMasterHelper.SHOW_ORDER_WEIGHT) {
                    holder.tvWeight.setVisibility(View.GONE);
                    holder.label_weight.setVisibility(View.GONE);
                }
                if (!businessModel.configurationMasterHelper.IS_FOCUSBRAND_COUNT_IN_REPORT) {
                    holder.tvFocusBrandCount.setVisibility(View.GONE);
                    holder.label_focusBrand.setVisibility(View.GONE);
                    holder.focus_brand_count1.setVisibility(View.GONE);
                }

                if (!businessModel.configurationMasterHelper.IS_MUSTSELL_COUNT_IN_REPORT) {
                    holder.text_mustSellCount.setVisibility(View.GONE);
                    holder.mustselllabel.setVisibility(View.GONE);
                    holder.text_mustSellCount.setVisibility(View.GONE);
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

                if(!businessModel.configurationMasterHelper.SHOW_VOLUME_QTY){
                    holder.tvVolumeLabel.setVisibility(View.GONE);
                    holder.tvVolumeValue.setVisibility(View.GONE);
                }

                if (!businessModel.configurationMasterHelper.SHOW_TOTAL_LINES) {
                    holder.text_LPC.setVisibility(View.GONE);
                    holder.label_LPC.setVisibility(View.GONE);
                }

                if (!businessModel.configurationMasterHelper.IS_DIST_PRE_POST_ORDER) {
                    holder.label_PreORPost.setVisibility(View.GONE);
                    holder.tvwDist.setVisibility(View.GONE);

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
            holder.tvVolumeValue.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
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
            holder.tvVolumeLabel.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));


            try {
                if (businessModel.labelsMasterHelper.applyLabels(holder.tvMustSellCount.getTag()) != null) {
                    String value = businessModel.labelsMasterHelper.applyLabels(holder.tvMustSellCount.getTag()) + " : " + reportBO.getMustSellCount();
                    holder.tvMustSellCount.setText(value);
                } else {
                    String value = getResources().getString(R.string.must_sell) + " : " + reportBO.getMustSellCount();
                    holder.tvMustSellCount.setText(value);
                    holder.text_mustSellCount.setText(String.valueOf(reportBO.getMustSellCount()));

                }
                if (businessModel.labelsMasterHelper.applyLabels(holder.tvFocusBrandCount.getTag()) != null) {
                    String value = businessModel.labelsMasterHelper.applyLabels(holder.tvFocusBrandCount.getTag()) + " : " + reportBO.getFocusBrandCount();
                    holder.tvFocusBrandCount.setText(value);
                } else {
                    String value = getResources().getString(R.string.focus_brand) + " : " + reportBO.getFocusBrandCount();
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

            try {

                StringBuilder sb = new StringBuilder();
                String op = getResources().getString(R.string.item_piece);
                String oc = getResources().getString(R.string.item_case);
                String ou = getResources().getString(R.string.item_outer);

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

                    sb.append(reportBO.getVolumePcsQty() + " " + op + " ");
                }
                if (businessModel.configurationMasterHelper.SHOW_ORDER_CASE) {

                    if (businessModel.configurationMasterHelper.SHOW_ORDER_PCS)
                        sb.append("\n" + (reportBO.getVolumeCaseQty()) + " "
                                + oc + " ");
                    else
                        sb.append(reportBO.getVolumeCaseQty() + " "
                                + oc + " ");
                }
                if (businessModel.configurationMasterHelper.SHOW_OUTER_CASE) {
                    if (businessModel.configurationMasterHelper.SHOW_ORDER_PCS || businessModel.configurationMasterHelper.SHOW_ORDER_CASE)
                        sb.append("\n" + (reportBO.getVolumeOuterQty()) + " "
                                + ou + " ");
                    else
                        sb.append(reportBO.getVolumeCaseQty() + " "
                                + ou + " ");
                }

                holder.tvVolumeValue.setText(sb.toString());

            } catch (Exception e) {
                e.printStackTrace();
            }


            if (businessModel.configurationMasterHelper.IS_SHOW_SELLER_DIALOG) {
                if (reportBO.getIsVanSeller() == 1)
                    holder.tv_seller_type.setText("V");
                else
                    holder.tv_seller_type.setText("P");
            } else {
                holder.tv_seller_type.setVisibility(View.INVISIBLE);
            }

            if (reportBO.getUpload().equalsIgnoreCase("Y")) {
                holder.text_retailerName.setTextColor(getResources().getColor(
                        R.color.GREEN));
                holder.text_orderValue.setTextColor(getResources().getColor(
                        R.color.GREEN));
                holder.text_LPC.setTextColor(getResources().getColor(
                        R.color.GREEN));
                holder.tvwDist.setTextColor(getResources().getColor(
                        R.color.GREEN));
                holder.tvOrderNo.setTextColor(getResources().getColor(
                        R.color.GREEN));

            } else {

                row.setBackgroundDrawable(getResources().getDrawable(
                        R.drawable.list_selector));
            }

            try {
                String delivery_date;

                delivery_date = DateUtil.convertFromServerDateToRequestedFormat(businessModel.getDeliveryDate(reportBO.getRetailerId()), ConfigurationMasterHelper.outDateFormat);

                if (businessModel.labelsMasterHelper.applyLabels(holder.text_delivery_date.getTag()) != null) {
                    String value = businessModel.labelsMasterHelper.applyLabels(holder.text_delivery_date.getTag()) + " : " + delivery_date;
                    holder.text_delivery_date.setText(value);
                } else {
                    String value = getResources().getString(R.string.delivery_date_label) + " : " + delivery_date;
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
    }

    class ViewHolder {
        TextView text_retailerName, label_orderNumber;
        TextView text_orderValue, text_LPC, tvwDist, tvWeight, label_LPC, label_PreORPost, focus_brand_count1, text_mustSellCount,tvVolumeValue;
        TextView text_delivery_date, tv_tax_value, tv_discount_amt, taxTitle, discTitle;
        ;
        TextView tvOrderNo, tvFocusBrandCount, tvMustSellCount, tv_seller_type, label_weight, label_focusBrand, label_MustSell, focusbrandlabel, mustselllabel,tvVolumeLabel;

    }

    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        try {
            OrderReportBO ret = list
                    .get(arg2);
            Intent intent = new Intent();
            intent.putExtra("OBJ",
                    ret);
            intent.putExtra("isFromOrder", true);
            intent.putExtra("TotalValue", ret.getOrderTotal());
            intent.putExtra("TotalLines", ret.getLPC());
            intent.setClass(getActivity(), OrderReportDetail.class);
            startActivityForResult(intent, 0);


        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public void onBackPressed() {

    }

    class XlsExport extends AsyncTask<Void, Void, Boolean> {

        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            builder = new AlertDialog.Builder(getActivity());

            customProgressDialog(builder, getResources().getString(R.string.exporting_orders));
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {

                ArrayList<String> columnNames = new ArrayList<>();
                columnNames.add("Distributor");
                columnNames.add("UserCode");
                columnNames.add("UserName");
                columnNames.add("RetailerCode");
                columnNames.add("RetailerName");
                columnNames.add("OrderNo");
                columnNames.add("OrderDate");
                columnNames.add("SKUCode");
                columnNames.add("SKUDescription");
                if (businessModel.configurationMasterHelper.SHOW_ORDER_PCS)
                    columnNames.add("OrderQty(Piece)");
                if (businessModel.configurationMasterHelper.SHOW_ORDER_CASE)
                    columnNames.add("OrderQty(Case)");
                if (businessModel.configurationMasterHelper.SHOW_OUTER_CASE)
                    columnNames.add("OrderQty(Outer)");
                columnNames.add("DeliveryDate");

                businessModel.reportHelper
                        .downloadOrderReportToExport();
                HashMap<String, ArrayList<ArrayList<String>>> mOrderDetailsByDistributorName = businessModel.reportHelper
                        .getmOrderDetailsByDistributorName();


                for (String distributorName : mOrderDetailsByDistributorName.keySet()) {

                    ArrayList<JExcelHelper.ExcelBO> mExcelBOList = new ArrayList<>();
                    JExcelHelper.ExcelBO excel = businessModel.mJExcelHelper.new ExcelBO();
                    excel.setSheetName(distributorName);
                    excel.setColumnNames(columnNames);
                    excel.setColumnValues(mOrderDetailsByDistributorName.get(distributorName));
                    mExcelBOList.add(excel);
                    businessModel.mJExcelHelper.createExcel("OrderReport_" + distributorName + ".xls", mExcelBOList);
                }

                if (businessModel.configurationMasterHelper.IS_ORDER_REPORT_EXPORT_AND_EMAIL)
                    businessModel.reportHelper.downloadOrderEmailAccountCredentials();


            } catch (Exception e) {
                Commons.printException(e);
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);


            alertDialog.dismiss();
            if (result) {
                Toast.makeText(getActivity(), getResources().getString(R.string.successfully_exported),
                        Toast.LENGTH_SHORT).show();

                try {

                    if (businessModel.configurationMasterHelper.IS_ORDER_REPORT_EXPORT_AND_SHARE) {
                        Intent sharingIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);

                        ArrayList<Uri> uriList = new ArrayList<>();
                        for (String distributorName : businessModel.reportHelper
                                .getmOrderDetailsByDistributorName().keySet()) {
                            File newFile = new File(getActivity().getExternalFilesDir(null) + "", "OrderReport_" + distributorName + ".xls");
                            if (Build.VERSION.SDK_INT >= 24) {
                                uriList.add(FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID + ".provider", newFile));

                            } else {
                                uriList.add(Uri.fromFile(newFile));
                            }
                        }

                        sharingIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList);

                        sharingIntent.setType("application/excel");
                        sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        sharingIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        //sharingIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{""});

                        startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_order_report_using)));
                    } else if (businessModel.configurationMasterHelper.IS_ORDER_REPORT_EXPORT_AND_EMAIL) {

                        if (businessModel.reportHelper.getUserName() != null && !businessModel.reportHelper.getUserName().equals("")
                                && businessModel.reportHelper.getUserPassword() != null && !businessModel.reportHelper.getUserPassword().equals(""))
                            new SendMail(getActivity()
                                    , "Order Report", "PFA").execute();
                        else
                            Toast.makeText(getActivity(), getResources().getString(R.string.invalid_credentials_mail_not_sent), Toast.LENGTH_LONG).show();


                    }
                } catch (Exception ex) {
                    Commons.printException(ex);
                }
            } else
                Toast.makeText(getActivity(), getResources().getString(R.string.export_failed),
                        Toast.LENGTH_SHORT).show();


        }

    }

    public class SendMail extends AsyncTask<Void, Void, Boolean> {

        Session session;

        Context mContext;
        private String subject;
        private String body;


        ProgressDialog progressDialog;

        public SendMail(Context ctx, String subject, String message) {
            this.mContext = ctx;

            this.subject = subject;
            this.body = message;


        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(mContext, getResources().getString(R.string.sending_email), getResources().getString(R.string.please_wait_some_time), false);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {

            Properties props = System.getProperties();// new Properties();

            //Configuring properties for G-MAIL
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.socketFactory.port", "587");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.starttls.enable", "true");

            //Creating a new session
            session = Session.getDefaultInstance(props,
                    new javax.mail.Authenticator() {
                        //Authenticating the password
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(businessModel.reportHelper.getUserName(), businessModel.reportHelper.getUserPassword());
                        }
                    });

            try {

                // sending distributor wise..
                for (String distributorName : businessModel.reportHelper
                        .getmOrderDetailsByDistributorName().keySet()) {

                    //not allowed if email not available
                    if (businessModel.reportHelper.getmEmailIdByDistributorName().get(distributorName) != null) {

                        Message message = new MimeMessage(session);
                        message.setFrom(new InternetAddress(businessModel.reportHelper.getUserName()));
                        message.setRecipient(Message.RecipientType.TO, new InternetAddress(businessModel.reportHelper.getmEmailIdByDistributorName().get(distributorName)));
                        message.setSubject(subject);
                        message.setText(body);
                        //  mm.setContent(message,"text/html; charset=utf-8");

                        BodyPart bodyPart = new MimeBodyPart();
                        bodyPart.setText(body);//Content(message,"text/html");

                        //Attachment
                        DataSource source = new FileDataSource(getActivity().getExternalFilesDir(null) + "/" + "OrderReport_" + distributorName + ".xls");
                        bodyPart.setDataHandler(new DataHandler(source));
                        bodyPart.setFileName("OrderReport_" + distributorName + ".xls");

                        MimeMultipart multiPart = new MimeMultipart();
                        multiPart.addBodyPart(bodyPart);
                        message.setContent(multiPart);

                        Thread.currentThread().setContextClassLoader(getActivity().getClassLoader());

                        MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
                        mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
                        mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
                        mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
                        mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
                        mc.addMailcap("message/rfc822;; x-java-content- handler=com.sun.mail.handlers.message_rfc822");

                        //sending mail
                        Transport.send(message);
                    }
                }
            } catch (Exception ex) {
                Commons.printException(ex);
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean isSent) {
            super.onPostExecute(isSent);

            progressDialog.dismiss();

            if (isSent) {
                Toast.makeText(getActivity(), getResources().getString(R.string.email_sent),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), getResources().getString(R.string.error_in_sending_email),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }


    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case 0:
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle("Do you want to delete Invoice")
                        .setPositiveButton(getResources().getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {

                                    }
                                })
                        .setNegativeButton(
                                getResources().getString(R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                    }
                                });
                businessModel.applyAlertDialogTheme(builder);
                break;

        }
        return null;
    }


}
