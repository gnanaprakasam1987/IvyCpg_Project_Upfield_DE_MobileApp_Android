package com.ivy.sd.png.view.reports;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.InvoiceReportBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.DateUtil;

import java.util.Vector;

public class InvoiceReportFragment extends IvyBaseFragment implements
        OnClickListener, OnItemClickListener {
    private TextView totalOrderValue, averageLines, mlpc, mavg_pre_post,totalqtyTV,outid,invoicenotitle,totalvaluetitle,lbl_total_qty,lbl_avg_lines;
    private ListView lvwplist;
    private Button xlsExport;
    private BusinessModel bmodel;
    private Vector<InvoiceReportBO> mylist;

    private int retailerid = 0;
    private double totalamount;
    private String minvoiceid = "";
    private InvoiceReportBO mSelectedInvoiceReportBO;
    private boolean isClicked;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

       View view = inflater.inflate(R.layout.fragment_invoice_report_new,
               container, false);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

        totalOrderValue = (TextView) view.findViewById(R.id.txttotal);
        averageLines = (TextView) view.findViewById(R.id.txtavglines);
        outid=(TextView) view.findViewById(R.id.outid);
        invoicenotitle=(TextView) view.findViewById(R.id.invoicenotitle);
        mavg_pre_post = (TextView) view.findViewById(R.id.txt_dist_pre_post);
        mlpc = (TextView) view.findViewById(R.id.lpc);
        totalqtyTV=(TextView)view.findViewById(R.id.txttotalqty);
        totalvaluetitle=(TextView)view.findViewById(R.id.totalvaluetitle);
        lbl_total_qty=(TextView)view.findViewById(R.id.lbl_total_qty);
        lbl_avg_lines=(TextView)view.findViewById(R.id.lbl_avg_lines);
        totalvaluetitle.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        lbl_total_qty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        lbl_avg_lines.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));



        xlsExport = (Button) view.findViewById(R.id.xlsExport);
        lvwplist = (ListView) view.findViewById(R.id.lvwplist);
        lvwplist.setCacheColorHint(0);
        xlsExport.setOnClickListener(this);
        lvwplist.setOnItemClickListener(this);


        mylist = bmodel.reportHelper.downloadInvoicereport();
        updateOrderGrid();
        double avglinesorderbooking = 0;
        int totalQty = 0;
        if (bmodel.configurationMasterHelper.SHOW_LPC_ORDER) {
            for (InvoiceReportBO ret : mylist) {
                avglinesorderbooking = avglinesorderbooking
                        + ret.getLinespercall();
            }
            double totoutlets = bmodel.reportHelper
                    .getorderbookingCount("InvoiceMaster");
            double result = avglinesorderbooking / totoutlets;
            Commons.print("average lines,"+ result + " " + totoutlets + " "
                    + avglinesorderbooking);
            String resultS = result + "";
            if (resultS.equals(getResources().getString(R.string.nan))) {
                averageLines.setText("0");
            } else {
                averageLines.setText("" + SDUtil.roundIt(result, 2));
            }
        }

        xlsExport.setVisibility(View.GONE);

        if (!bmodel.configurationMasterHelper.SHOW_LPC_ORDER) {
            view.findViewById(R.id.lbl_avg_lines).setVisibility(View.GONE);
            averageLines.setVisibility(View.GONE);
            mlpc.setVisibility(View.GONE);
        } else {
            try {
                if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                        R.id.lpc).getTag()) != null)
                    ((TextView) view.findViewById(R.id.lpc))
                            .setText(bmodel.labelsMasterHelper.applyLabels(view
                                    .findViewById(R.id.lpc).getTag()));
            } catch (Exception e) {
                Commons.printException(e);
            }
        }
        if (!bmodel.configurationMasterHelper.SHOW_TOTAL_QTY_IN_ORDER_REPORT) {
            view.findViewById(R.id.lbl_total_qty).setVisibility(View.VISIBLE);
            totalqtyTV.setVisibility(View.VISIBLE);

        } else {
            try {
                if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                        R.id.lbl_total_qty).getTag()) != null)
                    ((TextView) view.findViewById(R.id.lpc))
                            .setText(bmodel.labelsMasterHelper.applyLabels(view
                                    .findViewById(R.id.lbl_total_qty).getTag()));
            } catch (Exception e) {
                Commons.printException(e);
            }
        }
        for (InvoiceReportBO ret : mylist)
            totalQty = totalQty
                    + ret.getQty();

        totalqtyTV.setText(totalQty+"");

        if (!bmodel.configurationMasterHelper.IS_DIST_PRE_POST_ORDER) {
            view.findViewById(R.id.lab_dist_pre_post).setVisibility(View.GONE);
           view.findViewById(R.id.txt_dist_pre_post).setVisibility(View.GONE);
            view.findViewById(R.id.dist).setVisibility(View.GONE);
            view.findViewById(R.id.view00).setVisibility(View.GONE);

        }
        if (!bmodel.configurationMasterHelper.REMOVE_INVOICE) {
            view.findViewById(R.id.cancel).setVisibility(View.GONE);
        }
        try {
            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.outna).getTag()) != null)
                ((TextView) view.findViewById(R.id.outna))
                        .setText(bmodel.labelsMasterHelper.applyLabels(view
                                .findViewById(R.id.outna).getTag()));
            ((TextView) view.findViewById(R.id.outna)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            outid.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            invoicenotitle.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));


        } catch (Exception e) {
            Commons.printException(e);
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
        // TODO Auto-generated method stub
        try {
            if (!isClicked) {

                InvoiceReportBO inv = (InvoiceReportBO) mylist.get(arg2);
                if (bmodel.reportHelper.hasInvoiceDetails(inv.getInvoiceNumber())) {
                    isClicked = true;
                    retailerid = SDUtil.convertToInt(inv.getRetailerId());
                    new LoginProgressTask().execute(new Integer[]{arg2});
                } else
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.no_products_exists),
                            Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            // TODO: handle exception
            Commons.printException(e);
        }
    }

    @Override
    public void onClick(View v) {
        Button vw = (Button) v;
        if (vw == xlsExport) {
            new XlsExport().execute();
        }
    }

    class XlsExport extends AsyncTask<Void, Void, Boolean> {

        //	private ProgressDialog progressDialogue;
        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            /*progressDialogue = ProgressDialog.show(getActivity(),
                    DataMembers.SD, "Exporting orders...", true, false);*/
            builder = new AlertDialog.Builder(getActivity());

            customProgressDialog(builder, "Exporting orders...");
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO Auto-generated method stub
            try {

            } catch (Exception e) {
                Commons.printException(e);
            }
            return Boolean.TRUE; // Return your real result here
        }

        @Override
        protected void onPostExecute(Boolean result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            //	progressDialogue.dismiss();
            alertDialog.dismiss();
            if (result)
                Toast.makeText(getActivity(), "Sucessfully Exported.",
                        Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getActivity(), "Export Failed.",
                        Toast.LENGTH_SHORT).show();
        }

    }

    private void updateOrderGrid() {
        double totalvalue = 0;

        int pre = 0, post = 0;

        // Show alert if error loading data.
        if (mylist == null) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.unable_to_load_data),
                    Toast.LENGTH_SHORT).show();
            xlsExport.setVisibility(View.GONE);
            return;
        }
        // Show alert if no order exist.
        if (mylist.size() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.no_invoice_exist),
                    Toast.LENGTH_SHORT).show();
            xlsExport.setVisibility(View.GONE);
            return;
        }

        // Calculate the total order value.
        for (InvoiceReportBO ret : mylist) {

            totalvalue = totalvalue + SDUtil.convertToDouble(ret.getInvoiceAmount()+"");
        }
        if (bmodel.configurationMasterHelper.IS_DIST_PRE_POST_ORDER) {
            // Calculate the total order value.
            for (InvoiceReportBO ret : mylist) {
                try {
                    String str[] = ret.getDist().split("/");
                    pre = pre + Integer.parseInt(str[0]);
                    post = post + Integer.parseInt(str[1]);
                } catch (Exception e) {
                    // TODO: handle exception
                    Commons.printException(e);
                }

            }
            float preavg = 0, postavg = 0;
            if (mylist.size() > 0) {
                if (pre > 0) {
                    preavg = (float) pre / (float) mylist.size();
                }
                if (post > 0) {
                    postavg = (float) post / (float) mylist.size();
                }

                mavg_pre_post.setText(SDUtil.format(preavg, 1, 0) + "/"
                        + SDUtil.format(postavg, 1, 0));

            } else {
                mavg_pre_post.setText("0/0");
            }

        }
        // Format and set on the lable
        totalOrderValue.setText(bmodel.formatValue(totalvalue));

        // Load listview.
        MyAdapter mSchedule = new MyAdapter(mylist);
        lvwplist.setAdapter(mSchedule);
    }

    class MyAdapter extends ArrayAdapter<InvoiceReportBO> {
        Vector<InvoiceReportBO> items;

        private MyAdapter(Vector<InvoiceReportBO> items) {
            super(getActivity(), R.layout.row_invoice_report_new, items);
            this.items = items;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            InvoiceReportBO orderreport =  items.get(position);
            View row = convertView;

            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.row_invoice_report_new, parent, false);
                holder = new ViewHolder();
                holder.tvwrname = (TextView) row.findViewById(R.id.PRDNAME);

                holder.tvwvalue = (TextView) row.findViewById(R.id.PRDMRP);
                holder.tvwlpc = (TextView) row.findViewById(R.id.lpc);
                holder.tvwDist = (TextView) row.findViewById(R.id.dist_txt);
                holder.btnCancel = (Button) row.findViewById(R.id.btn_cancel);
                holder.tvinvoiceNo = (TextView) row.findViewById(R.id.invoice_number);
                holder.llcancel = (LinearLayout) row.findViewById(R.id.ll_cancel);
                holder.tvWeight = (TextView) row.findViewById(R.id.tv_weight);
                holder.tvTaxValue=(TextView)row.findViewById(R.id.tv_tax_value);
                holder.tvDiscValue=(TextView)row.findViewById(R.id.tv_priceoff_value);

                holder.lpctxt=(TextView)row.findViewById(R.id.lpctxt);
                holder.disttxtview=(TextView)row.findViewById(R.id.disttxtview);
                holder.tv_weighttxt=(TextView)row.findViewById(R.id.tv_weighttxt);
                holder.tv_tax_valuetxtview=(TextView)row.findViewById(R.id.tv_tax_valuetxtview);
                holder.tv_priceoff_valuetxt=(TextView)row.findViewById(R.id.tv_priceoff_valuetxt);

                ((View) row.findViewById(R.id.invoiceview_doted_line)).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                holder.lpctxt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.disttxtview.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.tv_weighttxt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.tv_tax_valuetxtview.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.tv_priceoff_valuetxt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.btnCancel.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        mSelectedInvoiceReportBO = holder.invoiceReportBO;
                        onCreateDialogNew(0).show();


                    }
                });
                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            holder.invoiceReportBO = orderreport;


            holder.tvwrname.setText(orderreport.getRetailerName());
            holder.tvwrname.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.tvinvoiceNo.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.tvwvalue.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.tvwlpc.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.tvwDist.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.tvWeight.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.tvTaxValue.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.tvDiscValue.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

            holder.tvwvalue.setText(bmodel.formatValue(orderreport
                    .getInvoiceAmount()));
            holder.tvwlpc.setText(orderreport.getLinespercall() + "");
            holder.tvwDist.setText(orderreport.getDist());
            holder.tvinvoiceNo.setText(holder.invoiceReportBO.getInvoiceNumber());
            holder.tvWeight.setText(holder.invoiceReportBO.getTotalWeight() + "");
            holder.tvTaxValue.setText(bmodel.formatValue(holder.invoiceReportBO.getTaxValue()));
            holder.tvDiscValue.setText(bmodel.formatValue(holder.invoiceReportBO.getDiscountValue()));
            if (!bmodel.configurationMasterHelper.SHOW_LPC_ORDER) {
                holder.tvwlpc.setVisibility(View.GONE);
                holder.lpctxt.setVisibility(View.GONE);
            }
            if (!bmodel.configurationMasterHelper.IS_DIST_PRE_POST_ORDER) {
                holder.tvwDist.setVisibility(View.GONE);
                holder.disttxtview.setVisibility(View.GONE);

            }
            if (holder.invoiceReportBO.getInvoicePaidAmount() > 0) {
                holder.btnCancel.setVisibility(View.GONE);
            } else {
                holder.btnCancel.setVisibility(View.VISIBLE);
            }
            if (!bmodel.configurationMasterHelper.REMOVE_INVOICE) {
                holder.btnCancel.setVisibility(View.GONE);
                holder.llcancel.setVisibility(View.GONE);
            }

            if (!bmodel.configurationMasterHelper.SHOW_ORDER_WEIGHT)
                holder.tvWeight.setVisibility(View.GONE);
                holder.tv_weighttxt.setVisibility(View.GONE);

            if(bmodel.configurationMasterHelper.IS_SHOW_TAX_IN_REPORT){
                holder.tvTaxValue.setVisibility(View.VISIBLE);
            }else{
                holder.tvTaxValue.setVisibility(View.VISIBLE);
            }

            if(bmodel.configurationMasterHelper.IS_SHOW_DISCOUNT_IN_REPORT)
            {
                holder.tvDiscValue.setVisibility(View.VISIBLE);
            }else{
                holder.tvDiscValue.setVisibility(View.VISIBLE);
            }

            // if (orderreport.getUpload().equalsIgnoreCase("Y")) {
            // holder.tvwrname.setTextColor(getResources().getColor(
            // R.color.GREEN));
            // holder.tvwvalue.setTextColor(getResources().getColor(
            // R.color.GREEN));
            // holder.tvwlpc.setTextColor(getResources().getColor(
            // R.color.GREEN));
            // holder.tvwDist.setTextColor(getResources().getColor(
            // R.color.GREEN));
            //
            // } else {
            //
            // row.setBackgroundDrawable(getResources().getDrawable(
            // R.drawable.list_selector));
            // }

            return (row);
        }
    }

    class ViewHolder {
        InvoiceReportBO invoiceReportBO;
        String ref;// product id
        TextView tvwrname;
        TextView tvwvol, tvwvalue, tvwlpc, tvwDist,tvTaxValue,tvDiscValue,lpctxt,disttxtview
            ,tv_weighttxt,tv_tax_valuetxtview,tv_priceoff_valuetxt;;
        TextView tvinvoiceNo;
        TextView tvWeight;
        Button btnCancel;
        LinearLayout llcancel;
    }

    class LoginProgressTask extends AsyncTask<Integer, Integer, Boolean> {

        //	private ProgressDialog progressDialogue;
        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {
                if(bmodel.configurationMasterHelper.COMMON_PRINT_BIXOLON||bmodel.configurationMasterHelper.COMMON_PRINT_SCRYBE||bmodel.configurationMasterHelper.COMMON_PRINT_ZEBRA){
                    InvoiceReportBO inv =  mylist.get(params[0]);
                    totalamount = inv.getInvoiceAmount();
                    bmodel.setOrderid(inv.getOrderID());
                    minvoiceid = inv.getInvoiceNumber();
                }else{
                    downloadRetailerMaster(retailerid);
                    //bmodel.productHelper.downloadProductFilter("");
                    if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER)
                        bmodel.productHelper.downloadProductsWithFiveLevelFilter("MENU_STK_ORD");
                    else bmodel.productHelper.downloadProducts("MENU_STK_ORD");

                    bmodel.schemeDetailsMasterHelper.downloadSchemeMethods();
//
                    InvoiceReportBO inv = (InvoiceReportBO) mylist.get(params[0]);
                    totalamount = inv.getInvoiceAmount();
                    bmodel.setInvoiceNumber(inv.getInvoiceNumber());
                    bmodel.loadInvoiceProducts(inv.getInvoiceNumber());

                    minvoiceid = inv.getInvoiceNumber();
                    bmodel.schemeDetailsMasterHelper.loadSchemeReportDetails(inv.getInvoiceNumber(), true);
                    bmodel.setInvoiceDate(new String(DateUtil.convertFromServerDateToRequestedFormat(SDUtil.now(SDUtil.DATE_GLOBAL), bmodel.configurationMasterHelper.outDateFormat)));
                    bmodel.batchAllocationHelper.loadOrderedBatchProducts(inv.getInvoiceNumber());
                    bmodel.batchAllocationHelper.downloadProductBatchCount();
                    if (bmodel.configurationMasterHelper.SHOW_DISCOUNT) {
                        bmodel.productHelper.downloadProductDiscountDetails();
                        bmodel.productHelper.downloadDiscountIdListByTypeId();

                    }
                    if (bmodel.configurationMasterHelper.SHOW_TAX_MASTER) {
                        bmodel.productHelper.downloadExcludeProductTaxDetails();
                        bmodel.productHelper.updateProductWiseTax();
                    }

                    bmodel.productHelper.updateBillWiseDiscountInObj(minvoiceid);


                    bmodel.setOrderid(inv.getOrderID());
                }


            } catch (Exception e) {
                Commons.printException(e);
            }
            return Boolean.TRUE; // Return your real result here
        }

        protected void onPreExecute() {
		/*	progressDialogue = ProgressDialog.show(getActivity(),
					DataMembers.SD, getResources().getString(R.string.loading),
					true, false);*/
            builder = new AlertDialog.Builder(getActivity());

            customProgressDialog(builder, getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {
            // result is the value returned from doInBackground
            //	progressDialogue.dismiss();

            alertDialog.dismiss();
            Intent intent = new Intent();
			/*
			 * if (bmodel.configurationMasterHelper.SHOW_BIXOLONI)
			 * intent.setClass(getActivity(), BixolonIPrint.class); else if
			 * (bmodel.configurationMasterHelper.SHOW_BIXOLONII)
			 * intent.setClass(getActivity(), BixolonIIPrint.class); else if
			 * (bmodel.configurationMasterHelper.SHOW_ZEBRA)
			 * intent.setClass(getActivity(), InvoicePrintZebraNew.class); else
			 * intent.setClass(getActivity(), BixolonIIPrint.class);
			 * intent.putExtra(getResources().getString(R.string.isfromreport),
			 * true);
			 */
            intent.putExtra("TotalAmount", totalamount);
            intent.putExtra("lineinvoice", minvoiceid);
            intent.setClass(getActivity(), InvoiceReportDetail.class);
            startActivityForResult(intent, 0);

        }

    }

    public void downloadRetailerMaster(int retailerid) {
        try {
            RetailerMasterBO retailer = new RetailerMasterBO();
            DBUtil db = new DBUtil(getActivity(), DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db
                    .selectSQL("select distinct A.retailerid, RPG.GroupId, A.subchannelid,(select ListCode from StandardListMaster where ListID = A.RpTypeId) as rp_type_code,"
                            + " A.RetailerCode, A.RetailerName, RA.Address1, A.tinnumber, A.Rfield3, RA.Address2, RA.Address3, A.TaxTypeId, A.locationid,A.Rfield2,A.isSameZone,A.GSTNumber from retailerMaster A"
                            + " LEFT JOIN RetailerPriceGroup RPG ON RPG.RetailerID = A.RetailerID"
                            + " LEFT JOIN RetailerAddress RA ON RA.RetailerId = A.RetailerID"
                            + " where A.retailerid="+ retailerid);
            if (c != null) {
                if (c.moveToNext()) {
                    retailer = new RetailerMasterBO();
                    retailer.setRetailerID(c.getString(0));
                    retailer.setGroupId(c.getInt(1));
                    retailer.setSubchannelid(c.getInt(2));
                    retailer.setRpTypeCode(c.getString(3));
                    retailer.setRetailerCode(c.getString(4));
                    retailer.setRetailerName(c.getString(5));
                    retailer.setAddress1(c.getString(6));
                    retailer.setTinnumber(c.getString(7));
                    retailer.setCredit_invoice_count(c.getString(8));
                    retailer.setAddress2(c.getString(9));
                    retailer.setAddress3(c.getString(10));
                    retailer.setTaxTypeId(c.getInt(c
                            .getColumnIndex("TaxTypeId")));
                    retailer.setLocationId(c.getInt(c
                            .getColumnIndex("locationid")));
                    retailer.setRfield2(c.getString(13));
                    retailer.setSameZone(c.getInt(14));
                    retailer.setGSTNumber(c.getString(15));

                }
                c.close();
            }

            bmodel.setRetailerMasterBO(retailer);
            db.closeDB();
        } catch (Exception e) {
        }
    }

    protected Dialog onCreateDialogNew(int id) {
        switch (id) {
            case 0:
                return new AlertDialog.Builder(getActivity())
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle("Do you want to delete Invoice?")
                        .setPositiveButton(getResources().getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        bmodel.reportHelper
                                                .deleteInvoiceDetail(mSelectedInvoiceReportBO);
                                        mylist = bmodel.reportHelper
                                                .downloadInvoicereport();
                                        if (mylist.size() > 0) {
                                            updateOrderGrid();
                                        } else {
                                            MyAdapter mSchedule = new MyAdapter(
                                                    mylist);
                                            lvwplist.setAdapter(mSchedule);
                                            Toast.makeText(
                                                    getActivity(),
                                                    getResources()
                                                            .getString(
                                                                    R.string.no_invoice_exist),
                                                    Toast.LENGTH_SHORT).show();
                                            xlsExport.setVisibility(View.GONE);
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
                //bmodel.applyAlertDialogTheme(builder);
                //break;
        }
        return null;
    }

}
