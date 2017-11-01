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
import com.ivy.sd.png.bo.InvoiceHeaderBO;
import com.ivy.sd.png.bo.InvoiceReportBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.DateUtil;

import java.util.ArrayList;
import java.util.Vector;

public class PndInvoiceReportFragment extends Fragment {
    private ListView lvwplist;
    private BusinessModel bmodel;
    private ArrayList<InvoiceHeaderBO> mylist;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        View view = inflater.inflate(R.layout.fragment_pnd_invoice_report,
                container, false);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

        lvwplist = (ListView) view.findViewById(R.id.lvwplist);
        lvwplist.setCacheColorHint(0);


        mylist = bmodel.getInvoiceHeaderBO();
        MyAdapter mSchedule = new MyAdapter(mylist);
        lvwplist.setAdapter(mSchedule);


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    class MyAdapter extends ArrayAdapter<InvoiceHeaderBO> {
        ArrayList<InvoiceHeaderBO> items;

        private MyAdapter(ArrayList<InvoiceHeaderBO> items) {
            super(getActivity(), R.layout.row_pnd_invoice_report, items);
            this.items = items;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            InvoiceHeaderBO invoiceHeaderBO = items.get(position);
            View row = convertView;

            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.row_pnd_invoice_report, parent, false);
                holder = new ViewHolder();

                ((View) row.findViewById(R.id.invoiceview_doted_line)).setLayerType(View.LAYER_TYPE_SOFTWARE, null);

                holder.tvRetailerName = (TextView) row.findViewById(R.id.tv_retailer_name);
                holder.tvInvoiceNo = (TextView) row.findViewById(R.id.invoice_number);
                holder.tvDate = (TextView) row.findViewById(R.id.tvDate);
                holder.tvInvAmount = (TextView) row.findViewById(R.id.tvinvamtValue);
                holder.tvAmtPaid = (TextView) row.findViewById(R.id.tvpaidamtValue);
                holder.tvBalance = (TextView) row.findViewById(R.id.tvbalamtValue);


                holder.tvRetailerName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.tvInvoiceNo.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                holder.tvDate.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                holder.tvInvAmount.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.tvAmtPaid.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.tvBalance.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                ((TextView) row.findViewById(R.id.tvinvamt)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                ((TextView) row.findViewById(R.id.tvinvamt)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                ((TextView) row.findViewById(R.id.tvinvamt)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));

                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            holder.invoiceReportBO = invoiceHeaderBO;

            holder.tvRetailerName.setText(holder.invoiceReportBO.getRetailerName());
            holder.tvInvoiceNo.setText(holder.invoiceReportBO.getInvoiceNo());
            holder.tvDate.setText(getResources().getString(R.string.inv_date) + ":" + holder.invoiceReportBO.getInvoiceDate());
            holder.tvInvAmount.setText(bmodel.formatValue(holder.invoiceReportBO.getInvoiceAmount()));
            holder.tvAmtPaid.setText(bmodel.formatValue(holder.invoiceReportBO.getPaidAmount()));
            holder.tvBalance.setText(bmodel.formatValue(holder.invoiceReportBO.getBalance()));

            return (row);
        }
    }

    class ViewHolder {
        InvoiceHeaderBO invoiceReportBO;
        TextView tvRetailerName, tvInvoiceNo, tvDate, tvInvAmount, tvAmtPaid, tvBalance;
    }


}
