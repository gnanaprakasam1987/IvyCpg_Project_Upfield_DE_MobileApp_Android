package com.ivy.sd.png.view;


import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;

public class ApprovedLeavesFragment extends IvyBaseFragment {

    BusinessModel bmodel;
    private String CODE_PENDING = "R", CODE_APPROVED = "S", CODE_REJECTED = "D";
    ArrayList<LeaveApprovalBO> approvedLeaves;
    private ListView lvLeavesList;
    private LinearLayout llFooter;
    private TextView tv_approve, tv_pending, tv_reject;
    private int selected_count = 0;
    LeaveApprovalBO leavesObj;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_leave_history,
                container, false);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        lvLeavesList = (ListView) view.findViewById(R.id.lv_leaves_list);
        llFooter = (LinearLayout) view.findViewById(R.id.lv_footer);
        tv_approve = (TextView) view.findViewById(R.id.tv_approve);
        tv_pending = (TextView) view.findViewById(R.id.tv_pending);
        tv_reject = (TextView) view.findViewById(R.id.tv_reject);
        tv_approve.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        tv_pending.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        tv_reject.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        selected_count = 0;
        tv_approve.setVisibility(View.GONE);
        llFooter.setVisibility(View.GONE);
        approvedLeaves = new ArrayList<>();
        for (LeaveApprovalBO leaves : bmodel.leaveApprovalHelper.getLeavePending()) {
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
                new SaveAsyncTask(CODE_PENDING).execute();
            }
        });

        tv_reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SaveAsyncTask(CODE_REJECTED).execute();
            }
        });


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
            leavesObj = (LeaveApprovalBO) items.get(position);
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getActivity());
                convertView = (View) inflater.inflate(
                        R.layout.row_leave_user_list, null);

                holder = new LeaveViewHolder();

                holder.tvUsername = (TextView) convertView.findViewById(R.id.tvusername);
                holder.tvLeavePeriod = (TextView) convertView.findViewById(R.id.tv_leaveperiod);
                holder.tv_leaveperiod_title = (TextView) convertView.findViewById(R.id.tv_leaveperiod_title);
                holder.tvLeaveType = (TextView) convertView.findViewById(R.id.tv_leavetype);
                holder.tv_leavetype_title = (TextView) convertView.findViewById(R.id.tv_leavetype_title);
                holder.tvStatus = (TextView) convertView.findViewById(R.id.tv_status);
                holder.tv_status_title = (TextView) convertView.findViewById(R.id.tv_status_title);
                holder.ll_userLeaves = (LinearLayout) convertView.findViewById(R.id.ll_userLeaves);
                holder.sel_img = (ImageView) convertView.findViewById(R.id.sel_img);

                holder.tvUsername.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
                holder.tvLeavePeriod.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                holder.tvLeaveType.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                holder.tvStatus.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                holder.tv_leaveperiod_title.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.tv_leavetype_title.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.tv_status_title.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));


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

    class SaveAsyncTask extends AsyncTask<Void, Integer, Boolean> {

        //	private ProgressDialog progressDialogue;
        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;
        private String status_code;

        SaveAsyncTask(String status_code) {
            this.status_code = status_code;
        }

        @Override
        protected Boolean doInBackground(Void... arg0) {

            for (int i = 0; i < approvedLeaves.size(); i++) {
                if (approvedLeaves.get(i).isSelected()) {
                    for (int j = 0; j < bmodel.leaveApprovalHelper.getLeavePending().size(); j++) {
                        if (approvedLeaves.get(i).getRefId() == bmodel.leaveApprovalHelper.getLeavePending().get(j).getRefId())
                            bmodel.leaveApprovalHelper.getLeavePending().get(j).setChanged(true);
                        bmodel.leaveApprovalHelper.getLeavePending().get(j).setStatusCode(status_code);
                    }
                }
            }
            try {
                bmodel.leaveApprovalHelper.saveStatusTransaction(bmodel.leaveApprovalHelper.getLeavePending());
                bmodel.leaveApprovalHelper.loadLeaveData();

                return Boolean.TRUE;
            } catch (Exception e) {
                Commons.printException(e);
                return Boolean.FALSE;
            }

        }

        protected void onPreExecute() {
            builder = new AlertDialog.Builder(getActivity());

            bmodel.customProgressDialog(alertDialog, builder, getActivity(), getResources().getString(R.string.saving));
            alertDialog = builder.create();
            alertDialog.show();

        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {
            try {
                if (alertDialog != null)
                    alertDialog.dismiss();
            } catch (Exception e) {
            }
            if (result == Boolean.TRUE) {

                Toast.makeText(
                        getActivity(),
                        getResources().getString(
                                R.string.saved_successfully),
                        Toast.LENGTH_SHORT).show();
                onStart();

            }

        }

    }
}
