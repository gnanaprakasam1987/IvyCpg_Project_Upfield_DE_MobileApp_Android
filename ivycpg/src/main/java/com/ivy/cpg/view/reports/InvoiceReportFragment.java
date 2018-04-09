package com.ivy.cpg.view.reports;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.cpg.view.order.OrderHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.InvoiceReportBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DateUtil;

import java.util.Vector;

/**
 * Invoice report main screen
 */
public class InvoiceReportFragment extends IvyBaseFragment implements
        OnClickListener, OnItemClickListener {

    private TextView totalOrderValue, mAveragePrePost;
    private ListView listView;

    private BusinessModel businessModel;
    private InvoiceReportBO mSelectedInvoiceReportBO;

    private Vector<InvoiceReportBO> list;

    private int mRetailerId = 0;
    private double mTotalAmount;
    private String mInvoiceId = "";
    private boolean isClicked;
    private OrderHelper orderHelper;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        View view = inflater.inflate(R.layout.fragment_invoice_report,
                container, false);

        businessModel = (BusinessModel) getActivity().getApplicationContext();
        businessModel.setContext(getActivity());
        orderHelper = OrderHelper.getInstance(getContext());

        if (businessModel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

        totalOrderValue = (TextView) view.findViewById(R.id.text_total);
        TextView text_averageLines = (TextView) view.findViewById(R.id.text_averageLines);
        mAveragePrePost = (TextView) view.findViewById(R.id.txt_dist_pre_post);
        TextView text_totalQuantity = (TextView) view.findViewById(R.id.text_totalQuantity);
        TextView label_totalValue = (TextView) view.findViewById(R.id.label_totalValue);
        TextView lbl_total_qty = (TextView) view.findViewById(R.id.lbl_total_qty);
        TextView lbl_avg_lines = (TextView) view.findViewById(R.id.lbl_avg_lines);
        label_totalValue.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        lbl_total_qty.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        lbl_avg_lines.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));


        listView = (ListView) view.findViewById(R.id.list);
        listView.setCacheColorHint(0);
        listView.setOnItemClickListener(this);


        list = businessModel.reportHelper.downloadInvoicereport();
        updateOrderGrid();

        double averageLines = 0;
        int totalQty = 0;
        if (businessModel.configurationMasterHelper.SHOW_LPC_ORDER) {
            for (InvoiceReportBO ret : list) {
                averageLines = averageLines
                        + ret.getLinespercall();
            }
            double totalOutlets = businessModel.reportHelper
                    .getorderbookingCount("InvoiceMaster");
            double result = averageLines / totalOutlets;
            Commons.print("average lines," + result + " " + totalOutlets + " "
                    + averageLines);
            String resultS = result + "";
            if (resultS.equals(getResources().getString(R.string.nan))) {
                text_averageLines.setText("0");
            } else {
                text_averageLines.setText(SDUtil.roundIt(result, 2));
            }
        }


        if (!businessModel.configurationMasterHelper.SHOW_LPC_ORDER) {
            view.findViewById(R.id.lbl_avg_lines).setVisibility(View.GONE);
            text_averageLines.setVisibility(View.GONE);
        } else {
            try {
                if (businessModel.labelsMasterHelper.applyLabels(view.findViewById(
                        R.id.lbl_avg_lines).getTag()) != null)
                    ((TextView) view.findViewById(R.id.lbl_avg_lines))
                            .setText(businessModel.labelsMasterHelper.applyLabels(view
                                    .findViewById(R.id.lbl_avg_lines).getTag()));
            } catch (Exception e) {
                Commons.printException(e);
            }
        }
        if (!businessModel.configurationMasterHelper.SHOW_TOTAL_QTY_IN_ORDER_REPORT) {
            view.findViewById(R.id.lbl_total_qty).setVisibility(View.VISIBLE);
            text_totalQuantity.setVisibility(View.VISIBLE);

        } else {
            try {
                if (businessModel.labelsMasterHelper.applyLabels(view.findViewById(
                        R.id.lbl_total_qty).getTag()) != null)
                    ((TextView) view.findViewById(R.id.lbl_total_qty))
                            .setText(businessModel.labelsMasterHelper.applyLabels(view
                                    .findViewById(R.id.lbl_total_qty).getTag()));
            } catch (Exception e) {
                Commons.printException(e);
            }
        }
        for (InvoiceReportBO ret : list)
            totalQty = totalQty
                    + ret.getQty();

        text_totalQuantity.setText(String.valueOf(totalQty));

        if (!businessModel.configurationMasterHelper.IS_DIST_PRE_POST_ORDER) {
            view.findViewById(R.id.lab_dist_pre_post).setVisibility(View.GONE);
            view.findViewById(R.id.txt_dist_pre_post).setVisibility(View.GONE);
            view.findViewById(R.id.view00).setVisibility(View.GONE);
        }


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        isClicked = false;
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        try {
            if (!isClicked) {

                InvoiceReportBO inv = list.get(arg2);
                if (businessModel.reportHelper.hasInvoiceDetails(inv.getInvoiceNumber())) {
                    isClicked = true;
                    mRetailerId = SDUtil.convertToInt(inv.getRetailerId());
                    new LoadInvoiceDetailAsyncTask().execute(arg2);
                } else
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.no_products_exists),
                            Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    @Override
    public void onClick(View v) {

    }

    private void updateOrderGrid() {
        double mTotalValue = 0;

        int pre = 0, post = 0;

        // Show alert if error loading data.
        if (list == null) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.unable_to_load_data),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        // Show alert if no order exist.
        if (list.size() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.no_invoice_exist),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Calculate the total order value.
        for (InvoiceReportBO ret : list) {

            mTotalValue = mTotalValue + SDUtil.convertToDouble(ret.getInvoiceAmount() + "");
        }
        if (businessModel.configurationMasterHelper.IS_DIST_PRE_POST_ORDER) {
            // Calculate the total order value.
            for (InvoiceReportBO ret : list) {
                try {
                    String str[] = ret.getDist().split("/");
                    pre = pre + Integer.parseInt(str[0]);
                    post = post + Integer.parseInt(str[1]);
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
                mAveragePrePost.setText(value);

            } else {
                mAveragePrePost.setText("0/0");
            }

        }
        // Format and set on the label
        totalOrderValue.setText(businessModel.formatValue(mTotalValue));

        // Load list view.
        MyAdapter mSchedule = new MyAdapter(list);
        listView.setAdapter(mSchedule);
    }


    /**
     * Alert dialog for delete
     * @return return dialog instance
     */
    protected Dialog deleteAlertDialog() {

        return new AlertDialog.Builder(getActivity())
                .setIcon(null)
                .setCancelable(false)
                .setTitle("Do you want to delete Invoice?")
                .setPositiveButton(getResources().getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                businessModel.reportHelper
                                        .deleteInvoiceDetail(mSelectedInvoiceReportBO);
                                list = businessModel.reportHelper
                                        .downloadInvoicereport();
                                if (list.size() > 0) {
                                    updateOrderGrid();
                                } else {
                                    MyAdapter mSchedule = new MyAdapter(
                                            list);
                                    listView.setAdapter(mSchedule);
                                    Toast.makeText(
                                            getActivity(),
                                            getResources()
                                                    .getString(
                                                            R.string.no_invoice_exist),
                                            Toast.LENGTH_SHORT).show();
                                }

                                mSelectedInvoiceReportBO = null;

                            }
                        })
                .setNegativeButton(
                        getResources().getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                            }
                        }).create();


    }

    /**
     * Invoice report adapter
     */
    class MyAdapter extends ArrayAdapter<InvoiceReportBO> {
        Vector<InvoiceReportBO> items;

        private MyAdapter(Vector<InvoiceReportBO> items) {
            super(getActivity(), R.layout.row_invoice_report_new, items);
            this.items = items;
        }

        @Override
        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            final ViewHolder holder;

            InvoiceReportBO invoiceReportBO = items.get(position);
            View row = convertView;

            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.row_invoice_report_new, parent, false);
                holder = new ViewHolder();
                holder.text_retailerName = (TextView) row.findViewById(R.id.PRDNAME);

                holder.text_value = (TextView) row.findViewById(R.id.PRDMRP);
                holder.text_LPC = (TextView) row.findViewById(R.id.lpc);
                holder.tvwDist = (TextView) row.findViewById(R.id.dist_txt);
                holder.btnCancel = (Button) row.findViewById(R.id.btn_cancel);
                holder.text_invoiceNumber = (TextView) row.findViewById(R.id.invoice_number);
                holder.layout_cancel = (LinearLayout) row.findViewById(R.id.ll_cancel);
                holder.tvWeight = (TextView) row.findViewById(R.id.tv_weight);
                holder.tvTaxValue = (TextView) row.findViewById(R.id.tv_tax_value);
                holder.tvDiscValue = (TextView) row.findViewById(R.id.tv_priceoff_value);

                holder.label_LPC = (TextView) row.findViewById(R.id.lpctxt);
                holder.label_PreOrPost = (TextView) row.findViewById(R.id.disttxtview);
                holder.label_weight = (TextView) row.findViewById(R.id.tv_weighttxt);
                holder.label_taxValue = (TextView) row.findViewById(R.id.tv_tax_valuetxtview);
                holder.label_discount = (TextView) row.findViewById(R.id.tv_priceoff_valuetxt);

                (row.findViewById(R.id.invoiceview_doted_line)).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                holder.label_LPC.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.label_PreOrPost.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.label_weight.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.label_taxValue.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.label_discount.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.btnCancel.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        mSelectedInvoiceReportBO = holder.invoiceReportBO;
                        deleteAlertDialog().show();


                    }
                });
                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            holder.invoiceReportBO = invoiceReportBO;


            holder.text_retailerName.setText(invoiceReportBO.getRetailerName());
            holder.text_retailerName.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.text_invoiceNumber.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.text_value.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.text_LPC.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.tvwDist.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.tvWeight.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.tvTaxValue.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.tvDiscValue.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

            holder.text_value.setText(businessModel.formatValue(invoiceReportBO
                    .getInvoiceAmount()));
            holder.text_LPC.setText(String.valueOf(invoiceReportBO.getLinespercall()));
            holder.tvwDist.setText(invoiceReportBO.getDist());
            holder.text_invoiceNumber.setText(holder.invoiceReportBO.getInvoiceNumber());
            holder.tvWeight.setText(String.valueOf(holder.invoiceReportBO.getTotalWeight()));
            holder.tvTaxValue.setText(businessModel.formatValue(holder.invoiceReportBO.getTaxValue()));
            holder.tvDiscValue.setText(businessModel.formatValue(holder.invoiceReportBO.getDiscountValue()));
            if (!businessModel.configurationMasterHelper.SHOW_LPC_ORDER) {
                holder.text_LPC.setVisibility(View.GONE);
                holder.label_LPC.setVisibility(View.GONE);
            }
            if (!businessModel.configurationMasterHelper.IS_DIST_PRE_POST_ORDER) {
                holder.tvwDist.setVisibility(View.GONE);
                holder.label_PreOrPost.setVisibility(View.GONE);

            }
            if (holder.invoiceReportBO.getInvoicePaidAmount() > 0) {
                holder.btnCancel.setVisibility(View.GONE);
            } else {
                holder.btnCancel.setVisibility(View.VISIBLE);
            }
            if (!businessModel.configurationMasterHelper.REMOVE_INVOICE) {
                holder.btnCancel.setVisibility(View.GONE);
                holder.layout_cancel.setVisibility(View.GONE);
            }

            if (!businessModel.configurationMasterHelper.SHOW_ORDER_WEIGHT)
                holder.tvWeight.setVisibility(View.GONE);
            holder.label_weight.setVisibility(View.GONE);

            if (businessModel.configurationMasterHelper.IS_SHOW_TAX_IN_REPORT) {
                holder.tvTaxValue.setVisibility(View.VISIBLE);
            } else {
                holder.tvTaxValue.setVisibility(View.VISIBLE);
            }

            if (businessModel.configurationMasterHelper.IS_SHOW_DISCOUNT_IN_REPORT) {
                holder.tvDiscValue.setVisibility(View.VISIBLE);
            } else {
                holder.tvDiscValue.setVisibility(View.VISIBLE);
            }


            return (row);
        }
    }

    class ViewHolder {
        InvoiceReportBO invoiceReportBO;
        TextView text_retailerName;
        TextView text_value, text_LPC, tvwDist, tvTaxValue, tvDiscValue, label_LPC, label_PreOrPost, label_weight, label_taxValue, label_discount;
        TextView text_invoiceNumber;
        TextView tvWeight;
        Button btnCancel;
        LinearLayout layout_cancel;
    }

    /**
     * Prepare data for invoice detail screen and move to it
     */
    class LoadInvoiceDetailAsyncTask extends AsyncTask<Integer, Integer, Boolean> {

        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {
                if (businessModel.configurationMasterHelper.COMMON_PRINT_BIXOLON || businessModel.configurationMasterHelper.COMMON_PRINT_SCRYBE || businessModel.configurationMasterHelper.COMMON_PRINT_ZEBRA) {
                    InvoiceReportBO inv = list.get(params[0]);
                    mTotalAmount = inv.getInvoiceAmount();
                    orderHelper.setOrderId(inv.getOrderID());
                    mInvoiceId = inv.getInvoiceNumber();
                } else {
                      businessModel.reportHelper.downloadRetailerMaster(getActivity().getApplicationContext(),mRetailerId);
                    if (businessModel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER)
                        businessModel.productHelper.downloadProductsWithFiveLevelFilter("MENU_STK_ORD");
                    else businessModel.productHelper.downloadProducts("MENU_STK_ORD");

                    businessModel.schemeDetailsMasterHelper.downloadSchemeMethods();

                    InvoiceReportBO inv = list.get(params[0]);
                    mTotalAmount = inv.getInvoiceAmount();
                    businessModel.setInvoiceNumber(inv.getInvoiceNumber());
                    orderHelper.loadInvoiceProducts(getActivity(), inv.getInvoiceNumber());

                    mInvoiceId = inv.getInvoiceNumber();
                    businessModel.schemeDetailsMasterHelper.loadSchemeReportDetails(inv.getInvoiceNumber(), true);
                    businessModel.setInvoiceDate(DateUtil.convertFromServerDateToRequestedFormat(SDUtil.now(SDUtil.DATE_GLOBAL), ConfigurationMasterHelper.outDateFormat));
                    businessModel.batchAllocationHelper.loadOrderedBatchProducts(inv.getInvoiceNumber());
                    businessModel.batchAllocationHelper.downloadProductBatchCount();
                    if (businessModel.configurationMasterHelper.SHOW_DISCOUNT) {
                        businessModel.productHelper.downloadProductDiscountDetails();
                        businessModel.productHelper.downloadDiscountIdListByTypeId();

                    }
                    if (businessModel.configurationMasterHelper.SHOW_TAX_MASTER) {

                        businessModel.productHelper.taxHelper.downloadProductTaxDetails();
                        if(businessModel.configurationMasterHelper.IS_EXCLUDE_TAX)
                            businessModel.productHelper.taxHelper.updateProductWiseExcludeTax();
//                        else
//                            businessModel.productHelper.taxHelper.updateProductWiseIncludeTax()
                    }

                    businessModel.productHelper.updateBillWiseDiscountInObj(mInvoiceId);

                    orderHelper.setOrderId(inv.getOrderID());
                    //load currency data
                    if (businessModel.configurationMasterHelper.IS_FORMAT_USING_CURRENCY_VALUE) {
                        businessModel.downloadCurrencyConfig();
                    }
                }


            } catch (Exception e) {
                Commons.printException(e);
            }
            return Boolean.TRUE; // Return your real result here
        }

        protected void onPreExecute() {
            builder = new AlertDialog.Builder(getActivity());
            customProgressDialog(builder, getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {

            alertDialog.dismiss();
            Intent intent = new Intent();
            intent.putExtra("TotalAmount", mTotalAmount);
            intent.putExtra("lineinvoice", mInvoiceId);
            intent.setClass(getActivity(), InvoiceReportDetail.class);
            startActivityForResult(intent, 0);

        }

    }

}
