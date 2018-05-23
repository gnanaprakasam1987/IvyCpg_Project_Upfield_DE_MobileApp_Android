package com.ivy.sd.png.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.SyncStatusBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;

import java.util.ArrayList;

/**
 * Created by anbarasan on 25/4/18.
 */

public class SyncStatusReportFragment extends IvyBaseFragment {

    View view;
    BusinessModel bmodel;
    private RecyclerView rvPerformance;


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        view = inflater.inflate(R.layout.fragment_sync_status, container, false);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onStart() {
        super.onStart();
        setHasOptionsMenu(true);
        initializeViews();
        bmodel.reportHelper.downloadSyncStatusReport();

        SyncStatusAdapter adapter=new SyncStatusAdapter(bmodel.reportHelper.getmSyncStatusBOList());
        rvPerformance.setAdapter(adapter);
    }

    private void initializeViews() {
        rvPerformance = (RecyclerView) view.findViewById(R.id.rvPerformance);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        rvPerformance.setLayoutManager(mLayoutManager);
        rvPerformance.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();

        if (i == android.R.id.home) {
            onBackButtonClick();
        }

        return super.onOptionsItemSelected(item);
    }

    private void onBackButtonClick() {
        Intent i = new Intent(getActivity(), HomeScreenActivity.class);
        i.putExtra("menuCode", "MENU_SYNC");
        i.putExtra("title", "Sync");
        startActivity(i);
        getActivity().finish();
        getActivity().overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
    }


    public class SyncStatusAdapter extends RecyclerView.Adapter<SyncStatusAdapter.MyViewHolder> {

        private ArrayList<SyncStatusBO> statusList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvTransactionId, tvTransactionTableName, tvLineCount;
            public CardView dateCardview;


            public MyViewHolder(View view) {
                super(view);
                tvTransactionId = (TextView) view.findViewById(R.id.idValueTv);
                tvTransactionTableName = (TextView) view.findViewById(R.id.transactionValue);
                tvLineCount = (TextView) view.findViewById(R.id.lineCountValue);
                dateCardview=(CardView) view.findViewById(R.id.dateCardview);

                tvTransactionId.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                tvTransactionTableName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                tvLineCount.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

            }
        }

        public SyncStatusAdapter(ArrayList<SyncStatusBO> statusList) {
            this.statusList = statusList;
        }

        @Override
        public SyncStatusAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.sync_status_row_item, parent, false);

            return new SyncStatusAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(SyncStatusAdapter.MyViewHolder holder, final int position) {
            final SyncStatusBO syncStatusBO = statusList.get(position);

            holder.tvTransactionId.setText(syncStatusBO.getId());
            holder.tvTransactionTableName.setText(syncStatusBO.getName());
            holder.tvLineCount.setText(syncStatusBO.getCount()+"");

            if(syncStatusBO.getShowDateTime()==1){
                holder.dateCardview.setVisibility(View.VISIBLE);
            }else{
                holder.dateCardview.setVisibility(View.GONE);
            }

        }

        @Override
        public int getItemCount() {
            return statusList.size();
        }
    }




}


