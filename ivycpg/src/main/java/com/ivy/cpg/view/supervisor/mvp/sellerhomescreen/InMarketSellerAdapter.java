package com.ivy.cpg.view.supervisor.mvp.sellerhomescreen;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ivy.cpg.view.supervisor.mvp.SellerBo;
import com.ivy.cpg.view.supervisor.mvp.sellerdetailmap.SellerDetailMapActivity;
import com.ivy.sd.png.asean.view.R;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class InMarketSellerAdapter extends RecyclerView.Adapter<InMarketSellerAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<SellerBo> sellerArrayList = new ArrayList<>();
    private SellerMapHomePresenter sellerMapHomePresenter;

    InMarketSellerAdapter(Context context, ArrayList<SellerBo> sellerArrayList,SellerMapHomePresenter sellerMapHomePresenter){
        this.context = context;
        this.sellerArrayList = sellerArrayList;
        this.sellerMapHomePresenter = sellerMapHomePresenter;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView userName, retailerName,retailerVisit,target,covered;
        private LinearLayout routeLayout;

        public MyViewHolder(View view) {
            super(view);
            userName = view.findViewById(R.id.tv_user_name);
            retailerName = view.findViewById(R.id.tv_address);
            retailerVisit = view.findViewById(R.id.tv_start_time);
            target = view.findViewById(R.id.tv_target_outlet);
            covered = view.findViewById(R.id.tv_outlet_covered);

            routeLayout = view.findViewById(R.id.route_layout);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.map_seller_info_layout, parent, false);

        return new MyViewHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {

        holder.userName.setText(sellerArrayList.get(position).getUserName());
        if(sellerArrayList.get(position).getRetailerName() == null)
            holder.retailerName.setText(context.getResources().getString(R.string.last_vist) +" "+context.getResources().getString(R.string.yet_to_visit));
        else
            holder.retailerName.setText(context.getResources().getString(R.string.last_vist)+" "+sellerArrayList.get(position).getRetailerName());
        holder.retailerVisit.setText(context.getResources().getString(R.string.visit_time)+" "+convertTime(sellerArrayList.get(position).getInTime()));
        holder.target.setText(context.getResources().getString(R.string.targeted)+" "+sellerArrayList.get(position).getTarget());
        holder.covered.setText(context.getResources().getString(R.string.covered)+" "+sellerArrayList.get(position).getCovered());

        holder.routeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SellerDetailMapActivity.class);
                intent.putExtra("SellerId", sellerArrayList.get(position).getUserId());
                intent.putExtra("screentitle", sellerArrayList.get(position).getUserName());
                intent.putExtra("Date",sellerMapHomePresenter.getSelectedDate());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return sellerArrayList.size();
    }

    private String convertTime(long time){

        if(time != 0) {
            Date date = new Date(time);
            Format format = new SimpleDateFormat("hh:mm a", Locale.US);
            return format.format(date);
        }else
            return "";
    }
}