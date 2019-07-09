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
import com.ivy.sd.png.bo.ProductivityReportBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.ReportHelper;
import com.ivy.utils.DateTimeUtils;

import java.util.ArrayList;

/**
 * Created by maheswaran.m on 15-10-2015.
 */
public class ProductivityReport extends Fragment {

    private TextView tv_outletclass, tv_tc, tv_pc, tv_vpd, tv_nonpc;
    private ListView lvwplist;
    private ArrayList<ProductivityReportBO> mylist;
    private BusinessModel bmodel;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        view = inflater.inflate(R.layout.fragment_productivity_report, container,
                false);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

        tv_outletclass = (TextView) view.findViewById(R.id.tv_outlet_class);
        tv_pc = (TextView) view.findViewById(R.id.tv_productive_call);
        tv_vpd = (TextView) view.findViewById(R.id.tv_value_per_day);
        tv_tc = (TextView) view.findViewById(R.id.tv_total_call);
        tv_nonpc = (TextView) view.findViewById(R.id.tv_non_productive_call);

        tv_outletclass.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        tv_pc.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        tv_vpd.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        tv_tc.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        tv_nonpc.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

        lvwplist = (ListView) view.findViewById(R.id.list);
        mylist = new ArrayList<ProductivityReportBO>();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mylist = ReportHelper.getInstance(getActivity()).downloadProductivitysReport(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL));

        // Show alert if error loading data.
        if (mylist == null) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.unable_to_load_data),
                    Toast.LENGTH_SHORT).show();
            return;
        } else if (mylist.size() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.no_visit_today),
                    Toast.LENGTH_SHORT).show();
            return;
        } else if (mylist != null) {
            MyAdapter mSchedule = new MyAdapter(mylist);
            lvwplist.setAdapter(mSchedule);
        }
    }

    class MyAdapter extends ArrayAdapter<ProductivityReportBO> {
        ArrayList<ProductivityReportBO> items;

        private MyAdapter(ArrayList<ProductivityReportBO> items) {
            super(getActivity(), R.layout.row_productivity_report, items);
            this.items = items;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            ProductivityReportBO mProductivityReportBO = (ProductivityReportBO) items
                    .get(position);
            View row = convertView;

            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater
                        .inflate(R.layout.row_productivity_report, parent, false);
                holder = new ViewHolder();

                holder.tv_outletclass = (TextView) row.findViewById(R.id.tv_outlet_class);
                holder.tv_pc = (TextView) row.findViewById(R.id.tv_productive_call);
                holder.tv_vpd = (TextView) row.findViewById(R.id.tv_value_per_day);
                holder.tv_tc = (TextView) row.findViewById(R.id.tv_total_call);
                holder.tv_nonpc = (TextView) row.findViewById(R.id.tv_non_productive_call);

                holder.tv_outletclass.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.tv_pc.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.tv_vpd.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.tv_nonpc.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }


            holder.tv_outletclass.setText(mProductivityReportBO.getmClassName());
            holder.tv_pc.setText(mProductivityReportBO.getmProductiveCall() + "");
            holder.tv_vpd.setText(mProductivityReportBO.getmOrderValue() + "");
            holder.tv_tc.setText(mProductivityReportBO.getmTotalCall() + "");
            int nonpcval = (mProductivityReportBO.getmTotalCall()) - (mProductivityReportBO.getmProductiveCall());
            holder.tv_nonpc.setText(nonpcval + "");

            return (row);
        }
    }

    class ViewHolder {
        private TextView tv_outletclass, tv_pc, tv_vpd, tv_tc, tv_nonpc;

    }
}
