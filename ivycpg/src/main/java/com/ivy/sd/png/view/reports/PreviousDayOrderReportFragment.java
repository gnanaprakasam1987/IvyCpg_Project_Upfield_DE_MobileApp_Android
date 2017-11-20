package com.ivy.sd.png.view.reports;

import android.app.AlertDialog;
import android.content.Intent;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ReportonorderbookingBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.JExcelHelper;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;

public class PreviousDayOrderReportFragment extends IvyBaseFragment implements
        OnClickListener, OnItemClickListener {

    private TextView totalOrderValue, averageLines, mavg_pre_post, totalLines;
    private ListView lvwplist;
    private Button xlsExport;
    private BusinessModel bmodel;
    private ArrayList<ReportonorderbookingBO> mylist;
    private View view;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        view = inflater.inflate(R.layout.fragment_order_report, container,
                false);

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
        mavg_pre_post = (TextView) view.findViewById(R.id.txt_dist_pre_post);
        totalLines = (TextView) view.findViewById(R.id.txttotallines);
        xlsExport = (Button) view.findViewById(R.id.xlsExport);
        lvwplist = (ListView) view.findViewById(R.id.lvwplist);
        lvwplist.setCacheColorHint(0);
        xlsExport.setOnClickListener(this);
        lvwplist.setOnItemClickListener(this);

        mylist = bmodel.reportHelper.downloadPVSOrderreport();
        updateOrderGrid();
        double avglinesorderbooking = bmodel.reportHelper
                .getavglinesfororderbooking("PVSOrderHeader");
        if (bmodel.configurationMasterHelper.SHOW_LPC_ORDER) {

            double totoutlets = bmodel.reportHelper
                    .getorderbookingCount("PVSOrderHeader");
            double result = avglinesorderbooking / totoutlets;
            String resultS = result + "";
            if (resultS.equals(getResources().getString(R.string.nan))) {
                averageLines.setText("" + 0);
            } else {
                averageLines.setText("" + SDUtil.roundIt(result, 2));
            }
        }
        if (bmodel.configurationMasterHelper.SHOW_TOTAL_LINES)
            totalLines.setText(avglinesorderbooking + "");

        if (!bmodel.configurationMasterHelper.SHOW_LPC_ORDER) {
            view.findViewById(R.id.lbl_avg_lines).setVisibility(View.GONE);
            averageLines.setVisibility(View.GONE);
            // view.findViewById(R.id.lpc).setVisibility(View.GONE);
        }

        if (!bmodel.configurationMasterHelper.IS_DIST_PRE_POST_ORDER) {
            view.findViewById(R.id.lab_dist_pre_post).setVisibility(View.GONE);
            view.findViewById(R.id.txt_dist_pre_post).setVisibility(View.GONE);
            view.findViewById(R.id.dist).setVisibility(View.GONE);
        }

        if (!bmodel.configurationMasterHelper.SHOW_TOTAL_LINES) {
            totalLines.setVisibility(View.GONE);
            view.findViewById(R.id.lbl_total_lines).setVisibility(View.GONE);

        }
        return view;
    }

    public void onClick(View comp) {
        Button vw = (Button) comp;
        if (vw == xlsExport) {
            if (bmodel.synchronizationHelper.isExternalStorageAvailable()) {
                new JExcelExport().execute();
            } else {
                Toast.makeText(
                        getActivity(),
                        getResources().getString(
                                R.string.external_storage_not_available),
                        Toast.LENGTH_SHORT).show();
            }
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
                    getResources().getString(R.string.no_orders_available),
                    Toast.LENGTH_SHORT).show();
            xlsExport.setVisibility(View.GONE);
            return;
        }

        // Calculate the total order value.
        for (ReportonorderbookingBO ret : mylist) {
            totalvalue = totalvalue + ret.getordertot();
        }

        if (!bmodel.configurationMasterHelper.SHOW_LPC_ORDER) {
            // Calculate the total order value.
            for (ReportonorderbookingBO ret : mylist) {
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
        totalOrderValue.setText("" + bmodel.formatValue(totalvalue));

        // Load listview.
        MyAdapter mSchedule = new MyAdapter(mylist);
        lvwplist.setAdapter(mSchedule);

    }

    class MyAdapter extends ArrayAdapter<ReportonorderbookingBO> {
        ArrayList<ReportonorderbookingBO> items;

        private MyAdapter(ArrayList<ReportonorderbookingBO> items) {
            super(getActivity(), R.layout.row_order_report, items);
            this.items = items;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            ReportonorderbookingBO orderreport = (ReportonorderbookingBO) items
                    .get(position);
            View row = convertView;

            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater
                        .inflate(R.layout.row_order_report, parent, false);
                holder = new ViewHolder();
                holder.tvwrname = (TextView) row.findViewById(R.id.PRDNAME);

                holder.tvwvalue = (TextView) row.findViewById(R.id.PRDMRP);
                holder.tvwlpc = (TextView) row.findViewById(R.id.PRDRP);
                holder.dist = (TextView) row.findViewById(R.id.dist_txt);
                holder.tvOrderNo = (TextView) row.findViewById(R.id.orderno);
                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            // if (!bmodel.configurationMasterHelper.SHOW_LPC_ORDER) {
            // holder.tvwlpc.setVisibility(View.GONE);
            // }

            if (!bmodel.configurationMasterHelper.IS_DIST_PRE_POST_ORDER) {
                holder.dist.setVisibility(View.GONE);

            }

            holder.tvwrname.setText(orderreport.getretailerName());
            holder.tvwvalue.setText(bmodel.formatValue((orderreport
                    .getordertot())) + "");
            holder.tvwlpc.setText(orderreport.getlpc());
            holder.dist.setText(orderreport.getDist());
            holder.tvOrderNo.setText(orderreport.getorderID());
            return (row);
        }
    }

    class ViewHolder {
        String ref;// product id
        TextView tvwrname;
        TextView tvwvol, tvwvalue, tvwlpc, dist, tvOrderNo;
    }

    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        // TODO Auto-generated method stub
        try {
            ReportonorderbookingBO ret = (ReportonorderbookingBO) mylist
                    .get(arg2);
            Intent orderreportdetail = new Intent();
            orderreportdetail.putExtra("OBJ",
                    ret);
            orderreportdetail.putExtra("TotalLines", ret.getlpc());
            orderreportdetail.putExtra("TotalValue", ret.getordertot());
            orderreportdetail.setClass(getActivity(), Orderreportdetail.class);
            startActivityForResult(orderreportdetail, 0);

			/*
             * FragmentTransaction ft=getFragmentManager().beginTransaction();
			 * ft.replace(R.id.realtabcontent, new
			 * OrderReportDetailFragment(),"orderdetail");
			 * ft.addToBackStack(null); ft.commit();
			 */

        } catch (Exception e) {
            // TODO: handle exception
            Commons.printException(e);
        }
    }

    public void onBackPressed() {
        // do something on back.
        return;
    }

    class JExcelExport extends AsyncTask<Void, Void, Boolean> {

        //private ProgressDialog progressDialogue;
        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            builder = new AlertDialog.Builder(getActivity());
            customProgressDialog(builder, getActivity(), "Exporting orders...");
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                ArrayList<String> columnNames = new ArrayList<String>();
                columnNames.add("SalesRepCode");
                columnNames.add("SalesRepName");
                columnNames.add("RetailerCode");
                columnNames.add("RetailerName");
                columnNames.add("OrderNo");
                columnNames.add("OrderDate");
                columnNames.add("SKUCode");
                columnNames.add("SKUDescription");
                columnNames.add("OrderQty(case)");
                columnNames.add("OrderQty(pcs)");
                columnNames.add("DeliveryDate");

                ArrayList<ArrayList<String>> columnValues = bmodel.reportHelper
                        .downloadPreviousOrderForExport();

                ArrayList<JExcelHelper.ExcelBO> mExcelBOList = new ArrayList<>();
                JExcelHelper.ExcelBO excel = bmodel.mJExcelHelper.new ExcelBO();
                excel.setSheetName("Orders");
                excel.setColumnNames(columnNames);
                excel.setColumnValues(columnValues);

                mExcelBOList.add(excel);

                bmodel.mJExcelHelper.createExcel("PreviousDayOrder.xls",mExcelBOList);

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

}
