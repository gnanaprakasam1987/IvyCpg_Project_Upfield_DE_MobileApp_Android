package com.ivy.sd.png.view;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ContractBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;

import java.util.ArrayList;
import java.util.Collections;

public class ContractReportFragment extends IvyBaseFragment {
    FrameLayout drawer;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contract_report, container,
                false);
        BusinessModel bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        ListView listView = (ListView)view.findViewById(R.id.listView);
        bmodel.reportHelper.downloadContractReport();
        if (bmodel.reportHelper.getContractList() != null) {
            Collections.sort(bmodel.reportHelper.getContractList(), ContractBO.DayToExpiryComparator);

            MyAdapter adapter = new MyAdapter(bmodel.reportHelper.getContractList());
            listView.setAdapter(adapter);
        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.data_not_mapped), Toast.LENGTH_SHORT).show();
        }

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();

        }
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    class MyAdapter extends BaseAdapter {
        ArrayList<ContractBO> arrayList;

        public MyAdapter(ArrayList<ContractBO> conList) {
            arrayList = conList;
        }

        @Override
        public int getCount() {
            return arrayList.size();
        }

        @Override
        public ContractBO getItem(int arg0) {
            return arrayList.get(arg0);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                convertView = inflater.inflate(
                        R.layout.contract_report_list_item, parent, false);
                holder = new ViewHolder();

                holder.snoTV = (TextView) convertView
                        .findViewById(R.id.tvSNO);
                holder.outletCodeTV = (TextView) convertView
                        .findViewById(R.id.tvOutletCode);
                holder.outletNameTV = (TextView) convertView
                        .findViewById(R.id.tvOutletName);
                holder.subChannelTV = (TextView) convertView
                        .findViewById(R.id.tvSubChannel);
                holder.contractIdTV = (TextView) convertView
                        .findViewById(R.id.tvContractId);
                holder.tradeTV = (TextView) convertView
                        .findViewById(R.id.tvTradeType);
                holder.startDateTV = (TextView) convertView
                        .findViewById(R.id.tvStartDate);
                holder.endDateTV = (TextView) convertView
                        .findViewById(R.id.tvEndDate);
                holder.noOfDaysTV = (TextView) convertView
                        .findViewById(R.id.tvNoOfDays);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            ContractBO contractBO = arrayList.get(position);
            int sno = position+1;
            holder.snoTV.setText(String.valueOf(sno));
            holder.outletCodeTV.setText(contractBO.getOutletCode());
            holder.outletNameTV.setText(contractBO.getOutletName());
            holder.subChannelTV.setText(contractBO.getSubChannel());
            holder.contractIdTV.setText(contractBO.getContractID());
            holder.tradeTV.setText(contractBO.getTradeName());
            holder.startDateTV.setText(contractBO.getStartDate());
            holder.endDateTV.setText(contractBO.getEndDate());
            holder.noOfDaysTV.setText(String.valueOf(contractBO.getDaysToExp()));
            return convertView;
        }

    }

    class ViewHolder {
        TextView snoTV;
        TextView outletCodeTV;
        TextView outletNameTV;
        TextView subChannelTV;
        TextView contractIdTV;
        TextView tradeTV;
        TextView startDateTV;
        TextView endDateTV;
        TextView noOfDaysTV;
    }
}
