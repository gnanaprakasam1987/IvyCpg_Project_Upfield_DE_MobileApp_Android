package com.ivy.ui.retailerplan.calendar.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.ui.retailerplan.addplan.DateWisePlanBo;
import com.ivy.ui.retailerplan.calendar.bo.CalenderBO;

import java.util.ArrayList;
import java.util.List;

public class BottmSheetRetailerInfoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context mContext;
    private List<DateWisePlanBo> mRetailerInfoList;

    public BottmSheetRetailerInfoAdapter(Context context, List<DateWisePlanBo> mRetailerInfoList) {
        mContext = context;
        this.mRetailerInfoList = mRetailerInfoList;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ;

        return new InfoViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.retailer_info_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {

        InfoViewHolder holder = (InfoViewHolder) viewHolder;
        holder.dateWisePlanBo = mRetailerInfoList.get(position);
        holder.tvRetailer.setText(holder.dateWisePlanBo.getName());
        holder.tvVisit.setText(mContext.getResources().getString(R.string.visit_hours, holder.dateWisePlanBo.getStartTime(),
                holder.dateWisePlanBo.getEndTime()));

    }


    class InfoViewHolder extends RecyclerView.ViewHolder {
        TextView tvRetailer, tvVisit;
        DateWisePlanBo dateWisePlanBo;

        InfoViewHolder(View itemView) {
            super(itemView);

            tvRetailer = itemView.findViewById(R.id.tv_retialer_name);
            tvVisit = itemView.findViewById(R.id.tv_visit_hours);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mRetailerInfoList.size();
    }

}