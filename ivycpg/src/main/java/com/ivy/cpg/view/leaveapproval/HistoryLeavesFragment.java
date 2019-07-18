package com.ivy.cpg.view.leaveapproval;


import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
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
import com.ivy.utils.FontUtils;

import java.util.ArrayList;

public class HistoryLeavesFragment extends IvyBaseFragment {

    private ListView lvLeavesList;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_leave_history,
                container, false);


        lvLeavesList = view.findViewById(R.id.lv_leaves_list);
        LinearLayout llFooter = view.findViewById(R.id.lv_footer);
        llFooter.setVisibility(View.GONE);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        LeaveApprovalHelper leaveApprovalHelper = LeaveApprovalHelper.getInstance(getActivity());

        if (leaveApprovalHelper.getLeaveApproved().size() > 0)
            lvLeavesList.setAdapter(new MyAdapter(leaveApprovalHelper.getLeaveApproved()));


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
        public @NonNull
        View getView(int position, View convertView, @NonNull ViewGroup viewGroup) {
            final LeaveViewHolder holder;
            String CODE_APPROVED = "S", CODE_REJECTED = "D";
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getActivity());
                convertView =  inflater.inflate(
                        R.layout.row_leave_user_list, viewGroup,false);

                holder = new LeaveViewHolder();
                holder.leaveApprovalBO = items.get(position);

                holder.tvUsername =  convertView.findViewById(R.id.tvusername);
                holder.tvLeavePeriod =  convertView.findViewById(R.id.tv_leaveperiod);
                holder.tv_leaveperiod_title =  convertView.findViewById(R.id.tv_leaveperiod_title);
                holder.tvLeaveType =  convertView.findViewById(R.id.tv_leavetype);
                holder.tv_leavetype_title =  convertView.findViewById(R.id.tv_leavetype_title);
                holder.tvStatus =  convertView.findViewById(R.id.tv_status);
                holder.tv_status_title =  convertView.findViewById(R.id.tv_status_title);

                holder.tvUsername.setTypeface(FontUtils.getFontBalooHai(getActivity(), FontUtils.FontType.REGULAR));
                holder.tvLeavePeriod.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));
                holder.tvLeaveType.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));
                holder.tvStatus.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));
                holder.tv_leaveperiod_title.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));
                holder.tv_leavetype_title.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));
                holder.tv_status_title.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));

                holder.tvUsername.setText(holder.leaveApprovalBO.getUsername());
                if (holder.leaveApprovalBO.getFromDate().equals(holder.leaveApprovalBO.getToDate()))
                    holder.tvLeavePeriod.setText(holder.leaveApprovalBO.getFromDate());
                else
                    holder.tvLeavePeriod.setText(holder.leaveApprovalBO.getFromDate() + " to " + holder.leaveApprovalBO.getToDate());
                holder.tvLeaveType.setText(holder.leaveApprovalBO.getReason());
                holder.tvStatus.setText(holder.leaveApprovalBO.getStatus());

                if (holder.leaveApprovalBO.getStatusCode().equals(CODE_APPROVED))
                    holder.tvStatus.setTextColor(ContextCompat.getColor(getActivity(), R.color.green_productivity));
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
