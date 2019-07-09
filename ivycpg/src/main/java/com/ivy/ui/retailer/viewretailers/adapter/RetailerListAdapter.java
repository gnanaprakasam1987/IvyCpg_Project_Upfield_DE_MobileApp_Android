package com.ivy.ui.retailer.viewretailers.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.RetailerMasterBO;

import java.util.List;

public class RetailerListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context mContext;
    private List<RetailerMasterBO> mRetailerList;
    private RetailerListClickListner retailerListClickListner;

    public RetailerListAdapter(Context context, List<RetailerMasterBO> mRetailerList,RetailerListClickListner retailerListClickListner) {
        mContext = context;
        this.mRetailerList = mRetailerList;
        this.retailerListClickListner = retailerListClickListner;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new RetailerListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.retailer_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {

        RetailerListViewHolder holder = (RetailerListViewHolder) viewHolder;
        holder.retailerMasterBO = mRetailerList.get(position);
        holder.tvRetailer.setText(holder.retailerMasterBO.getRetailerName());
        holder.tvLocation.setText(holder.retailerMasterBO.getAddress1());

        holder.tvPlanned.setText(String.valueOf(holder.retailerMasterBO.getTotalPlanned()));

        if (holder.retailerMasterBO.getTotalPlanned() > holder.retailerMasterBO.getVisit_frequencey())
            DrawableCompat.setTint(holder.tvPlanned.getBackground(), ContextCompat.getColor(mContext, R.color.rippelColor));
        else if (holder.retailerMasterBO.getTotalPlanned() < holder.retailerMasterBO.getVisit_frequencey())
            DrawableCompat.setTint(holder.tvPlanned.getBackground(), ContextCompat.getColor(mContext, R.color.colorPrimaryRed));

        holder.tvVisitFreq.setText(String.valueOf(holder.retailerMasterBO.getVisit_frequencey()));
        holder.tvVisitPlanned.setText(String.valueOf(holder.retailerMasterBO.getTotalVisited()));
        holder.tvVisitPending.setText(String.valueOf
                (holder.retailerMasterBO.getTotalPlanned() - holder.retailerMasterBO.getTotalVisited()));

        holder.rlRetailer.setOnClickListener(v -> {
            retailerListClickListner.onRetailerSelected(holder.retailerMasterBO);
        });
    }

    class RetailerListViewHolder extends RecyclerView.ViewHolder {
        TextView tvRetailer, tvPlanned, tvLocation, tvVisitFreq, tvVisitPlanned, tvVisitPending;
        RetailerMasterBO retailerMasterBO;
        RelativeLayout rlRetailer;

        RetailerListViewHolder(View itemView) {
            super(itemView);

            tvRetailer = itemView.findViewById(R.id.tv_retialer_name);
            tvPlanned = itemView.findViewById(R.id.tv_planned);
            tvLocation = itemView.findViewById(R.id.tv_location);
            tvVisitFreq = itemView.findViewById(R.id.tv_visit_freq);
            tvVisitPlanned = itemView.findViewById(R.id.tv_visit_planned);
            tvVisitPending = itemView.findViewById(R.id.tv_visit_pending);
            rlRetailer = itemView.findViewById(R.id.rl_retailer);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mRetailerList.size();
    }

}