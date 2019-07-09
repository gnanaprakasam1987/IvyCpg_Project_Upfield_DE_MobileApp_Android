package com.ivy.cpg.view.reports.piramal;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.RetailersReportBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.ReportHelper;

import java.util.ArrayList;

/**
 * Created by maheswaran.m on 08-10-2015.
 */


public class TopTenRetailers extends Fragment {

    private ListView lvwplist;
    private ArrayList<RetailersReportBO> mylist;
    private BusinessModel bmodel;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        view = inflater.inflate(R.layout.fragment_toptenretailers, container,
                false);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

        ((TextView) view.findViewById(R.id.tv_retailername)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) view.findViewById(R.id.tv_purchase_values)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) view.findViewById(R.id.tv_lines_value)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        lvwplist = (ListView) view.findViewById(R.id.list);
        mylist = new ArrayList<RetailersReportBO>();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mylist = ReportHelper.getInstance(getActivity()).downloadRetailersReport();

        // Show alert if error loading data.
        if (mylist == null) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.unable_to_load_data),
                    Toast.LENGTH_SHORT).show();
            return;
        } else if (mylist.size() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.no_orders_available),
                    Toast.LENGTH_SHORT).show();
            return;
        } else if (mylist != null) {
            MyAdapter mSchedule = new MyAdapter(mylist);
            lvwplist.setAdapter(mSchedule);
        }
    }

    class MyAdapter extends ArrayAdapter<RetailersReportBO> {
        ArrayList<RetailersReportBO> items;

        private MyAdapter(ArrayList<RetailersReportBO> items) {
            super(getActivity(), R.layout.row_toptenretailers_report, items);
            this.items = items;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            RetailersReportBO mRetailersReportBO = (RetailersReportBO) items
                    .get(position);
            View row = convertView;

            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater
                        .inflate(R.layout.row_toptenretailers_report, parent, false);
                holder = new ViewHolder();

                holder.vh_retailername = (TextView) row.findViewById(R.id.tv_retailername);
                holder.vh_purchasevalue = (TextView) row.findViewById(R.id.tv_purchase_values);
                holder.vh_linesavg = (TextView) row.findViewById(R.id.tv_lines_value);

                holder.vh_retailername.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.vh_purchasevalue.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.vh_linesavg.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }


            holder.vh_retailername.setText(mRetailersReportBO.getmRetailername());
            holder.vh_purchasevalue.setText(mRetailersReportBO.getmPurchaseValue() + "");
            holder.vh_linesavg.setText(mRetailersReportBO.getmLinesAvg() + "");

            return (row);
        }
    }

    class ViewHolder {
        private TextView vh_retailername, vh_purchasevalue, vh_linesavg;

    }

}
