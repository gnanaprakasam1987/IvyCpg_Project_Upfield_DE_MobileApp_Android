package com.ivy.cpg.view.leaveapproval;


import android.app.ProgressDialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.LeaveApprovalBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.FontUtils;
import com.ivy.utils.rx.AppSchedulerProvider;

import java.util.ArrayList;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

public class ApprovedLeavesFragment extends IvyBaseFragment {

    private ArrayList<LeaveApprovalBO> approvedLeaves;
    private ListView lvLeavesList;
    private LinearLayout llFooter;
    private TextView tv_approve, tv_pending, tv_reject;
    private int selected_count = 0;
    private LeaveApprovalHelper leaveApprovalHelper;
    private ProgressDialog progressDialogue;
    private AppSchedulerProvider appSchedulerProvider;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_leave_history,
                container, false);

        leaveApprovalHelper = LeaveApprovalHelper.getInstance(getActivity());
        appSchedulerProvider = new AppSchedulerProvider();

        lvLeavesList = view.findViewById(R.id.lv_leaves_list);
        llFooter = view.findViewById(R.id.lv_footer);
        tv_approve = view.findViewById(R.id.tv_approve);
        tv_pending = view.findViewById(R.id.tv_pending);
        tv_reject = view.findViewById(R.id.tv_reject);
        tv_approve.setTypeface(FontUtils.getFontBalooHai(getActivity(), FontUtils.FontType.REGULAR));
        tv_pending.setTypeface(FontUtils.getFontBalooHai(getActivity(), FontUtils.FontType.REGULAR));
        tv_reject.setTypeface(FontUtils.getFontBalooHai(getActivity(), FontUtils.FontType.REGULAR));

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        selected_count = 0;
        tv_approve.setVisibility(View.GONE);
        llFooter.setVisibility(View.GONE);
        approvedLeaves = new ArrayList<>();

        final String CODE_APPROVED = "S",CODE_PENDING = "R", CODE_REJECTED = "D";

        for (LeaveApprovalBO leaves : leaveApprovalHelper.getLeavePending()) {
            if (leaves.getStatusCode().equals(CODE_APPROVED)) {
                leaves.setSelected(false);
                approvedLeaves.add(leaves);
            }
        }

        if (approvedLeaves.size() > 0)
            lvLeavesList.setAdapter(new MyAdapter(approvedLeaves));
        else
            lvLeavesList.setVisibility(View.GONE);

        tv_pending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processLeave(CODE_PENDING);
            }
        });

        tv_reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processLeave(CODE_REJECTED);
            }
        });


    }

    private void processLeave(String status) {
        progressDialogue = ProgressDialog.show(getActivity(),
                DataMembers.SD, getResources().getString(R.string.saving),
                true, false);
        for (int i = 0; i < approvedLeaves.size(); i++) {
            if (approvedLeaves.get(i).isSelected()) {
                for (int j = 0; j < leaveApprovalHelper.getLeavePending().size(); j++) {
                    if (approvedLeaves.get(i).getRefId() == leaveApprovalHelper.getLeavePending().get(j).getRefId())
                        leaveApprovalHelper.getLeavePending().get(j).setChanged(true);
                    leaveApprovalHelper.getLeavePending().get(j).setStatusCode(status);
                }
            }
        }
        new CompositeDisposable().add(leaveApprovalHelper.updateLeaves()
                .subscribeOn(appSchedulerProvider.io())
                .observeOn(appSchedulerProvider.ui())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) {
                        progressDialogue.dismiss();

                        Toast.makeText(
                                getActivity(),
                                getResources().getString(
                                        R.string.saved_successfully),
                                Toast.LENGTH_SHORT).show();
                        onStart();
                    }
                }));

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
            LeaveApprovalBO leavesObj = items.get(position);
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getActivity());
                convertView = inflater.inflate(
                        R.layout.row_leave_user_list, viewGroup, false);

                holder = new LeaveViewHolder();

                holder.tvUsername = convertView.findViewById(R.id.tvusername);
                holder.tvLeavePeriod = convertView.findViewById(R.id.tv_leaveperiod);
                holder.tv_leaveperiod_title = convertView.findViewById(R.id.tv_leaveperiod_title);
                holder.tvLeaveType = convertView.findViewById(R.id.tv_leavetype);
                holder.tv_leavetype_title = convertView.findViewById(R.id.tv_leavetype_title);
                holder.tvStatus = convertView.findViewById(R.id.tv_status);
                holder.tv_status_title = convertView.findViewById(R.id.tv_status_title);
                holder.ll_userLeaves = convertView.findViewById(R.id.ll_userLeaves);
                holder.sel_img = convertView.findViewById(R.id.sel_img);

                holder.tvUsername.setTypeface(FontUtils.getFontBalooHai(getActivity(), FontUtils.FontType.REGULAR));
                holder.tvLeavePeriod.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));
                holder.tvLeaveType.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));
                holder.tvStatus.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));
                holder.tv_leaveperiod_title.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));
                holder.tv_leavetype_title.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));
                holder.tv_status_title.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));


                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (holder.leaveApprovalBO.isSelected()) {
                            holder.sel_img.setVisibility(View.GONE);
                            holder.leaveApprovalBO.setSelected(false);
                            if (selected_count > 0)
                                selected_count--;
                        } else {
                            holder.sel_img.setVisibility(View.VISIBLE);
                            holder.leaveApprovalBO.setSelected(true);
                            selected_count++;
                        }
                        if (selected_count > 0)
                            llFooter.setVisibility(View.VISIBLE);
                        else
                            llFooter.setVisibility(View.GONE);
                    }

                });

                convertView.setTag(holder);
            } else {
                holder = (LeaveViewHolder) convertView.getTag();
            }

            holder.leaveApprovalBO = leavesObj;
            holder.tvUsername.setText(holder.leaveApprovalBO.getUsername());
            if (holder.leaveApprovalBO.getFromDate().equals(holder.leaveApprovalBO.getToDate()))
                holder.tvLeavePeriod.setText(holder.leaveApprovalBO.getFromDate());
            else
                holder.tvLeavePeriod.setText(holder.leaveApprovalBO.getFromDate() + " to " + holder.leaveApprovalBO.getToDate());
            holder.tvLeaveType.setText(holder.leaveApprovalBO.getReason());

            holder.tvStatus.setText(holder.leaveApprovalBO.getStatus());

            if (holder.leaveApprovalBO.isSelected()) {
                holder.sel_img.setVisibility(View.VISIBLE);
            } else {
                holder.sel_img.setVisibility(View.GONE);
            }


            return convertView;
        }
    }


    static class LeaveViewHolder {
        TextView tvUsername, tvLeavePeriod, tv_leaveperiod_title, tvLeaveType, tv_leavetype_title, tvStatus, tv_status_title;
        LeaveApprovalBO leaveApprovalBO;
        LinearLayout ll_userLeaves;
        ImageView sel_img;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        leaveApprovalHelper.clearInstance();
    }
}
