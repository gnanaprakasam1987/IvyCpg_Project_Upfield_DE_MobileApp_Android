package com.ivy.sd.png.view;


import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.LeaveApprovalBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;

import java.util.ArrayList;

public class HistoryLeavesFragment extends IvyBaseFragment {

    BusinessModel bmodel;
    private ListView lvLeavesList;
    private String CODE_APPROVED = "S", CODE_REJECTED = "D";
    private LinearLayout llFooter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_leave_history,
                container, false);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        lvLeavesList = (ListView) view.findViewById(R.id.lv_leaves_list);
        llFooter = (LinearLayout) view.findViewById(R.id.lv_footer);
        llFooter.setVisibility(View.GONE);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();


        if (bmodel.leaveApprovalHelper.getLeaveApproved().size() > 0)
            lvLeavesList.setAdapter(new MyAdapter(bmodel.leaveApprovalHelper.getLeaveApproved()));


    }

    class MyAdapter extends ArrayAdapter<LeaveApprovalBO> {
        private ArrayList<LeaveApprovalBO> items;

        public MyAdapter(ArrayList<LeaveApprovalBO> items) {
            super(getActivity(), R.layout.row_leave_user_list);
            this.items = items;
        }


        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public LeaveApprovalBO getItem(int i) {
            return items.get(i);
        }

        @Override
        public long getItemId(int i) {
            return items.get(i).getRefId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            final LeaveViewHolder holder;
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getActivity());
                convertView = (View) inflater.inflate(
                        R.layout.row_leave_user_list, null);

                holder = new LeaveViewHolder();
                holder.leaveApprovalBO = items.get(position);

                holder.tvUsername = (TextView) convertView.findViewById(R.id.tvusername);
                holder.tvLeavePeriod = (TextView) convertView.findViewById(R.id.tv_leaveperiod);
                holder.tv_leaveperiod_title = (TextView) convertView.findViewById(R.id.tv_leaveperiod_title);
                holder.tvLeaveType = (TextView) convertView.findViewById(R.id.tv_leavetype);
                holder.tv_leavetype_title = (TextView) convertView.findViewById(R.id.tv_leavetype_title);
                holder.tvStatus = (TextView) convertView.findViewById(R.id.tv_status);
                holder.tv_status_title = (TextView) convertView.findViewById(R.id.tv_status_title);


                holder.tvUsername.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
                holder.tvLeavePeriod.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.tvLeaveType.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.tvStatus.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.tv_leaveperiod_title.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.tv_leavetype_title.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.tv_status_title.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

                holder.tvUsername.setText(holder.leaveApprovalBO.getUsername());
                if (holder.leaveApprovalBO.getFromDate().equals(holder.leaveApprovalBO.getToDate()))
                    holder.tvLeavePeriod.setText(holder.leaveApprovalBO.getFromDate());
                else
                    holder.tvLeavePeriod.setText(holder.leaveApprovalBO.getFromDate() + " to " + holder.leaveApprovalBO.getToDate());
                holder.tvLeaveType.setText(holder.leaveApprovalBO.getReason());
                holder.tvStatus.setText(holder.leaveApprovalBO.getStatus());

                if (holder.leaveApprovalBO.getStatusCode().equals(CODE_APPROVED))
                    holder.tvStatus.setTextColor(ContextCompat.getColor(getActivity(), R.color.GREEN));
                else if (holder.leaveApprovalBO.getStatusCode().equals(CODE_REJECTED))
                    holder.tvStatus.setTextColor(ContextCompat.getColor(getActivity(), R.color.RED));




            }

            return convertView;
        }
    }


    static class LeaveViewHolder {
        TextView tvUsername, tvLeavePeriod, tv_leaveperiod_title, tvLeaveType, tv_leavetype_title, tvStatus, tv_status_title;
        LeaveApprovalBO leaveApprovalBO;
    }


}
