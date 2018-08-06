package com.ivy.cpg.view.supervisor.mvp.sellerperformance.sellerperformancelist;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ivy.cpg.view.supervisor.mvp.SellerBo;
import com.ivy.sd.png.asean.view.R;
import com.ivy.utils.FontUtils;

import java.util.ArrayList;

public class SellerPerformanceListAdapter extends RecyclerView.Adapter<SellerPerformanceListAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<SellerBo> sellerList = new ArrayList<>();
    private ItemClickedListener itemClickedListener;

    SellerPerformanceListAdapter(Context context,ArrayList<SellerBo> sellerList,ItemClickedListener itemClickedListener){
        this.context = context;
        this.sellerList = sellerList;
        this.itemClickedListener = itemClickedListener;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView sellerNameTv,sellerPositionTv,sellerPerformancePercentTv;
        private ProgressBar progressBar;

        public MyViewHolder(View view) {
            super(view);
            sellerNameTv = view.findViewById(R.id.seller_name);
            sellerPositionTv = view.findViewById(R.id.seller_position);
            sellerPerformancePercentTv = view.findViewById(R.id.seller_perform_percent);
            progressBar = view.findViewById(R.id.progressBar);

            sellerNameTv.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,context));
            sellerPositionTv.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,context));
            sellerPerformancePercentTv.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM,context));
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.seller_performance_list_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        holder.sellerNameTv.setText(sellerList.get(holder.getAdapterPosition()).getUserName());

        int target = sellerList.get(holder.getAdapterPosition()).getTarget();
        int billed = sellerList.get(holder.getAdapterPosition()).getBilled();
        int sellerProductive = 0;

        if (target != 0) {
            sellerProductive = (int)((float)billed / (float)target * 100);
        }

        holder.sellerPerformancePercentTv.setText(sellerProductive+"%");
        holder.progressBar.setProgress(sellerProductive);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                itemClickedListener.itemclicked(sellerList.get(holder.getAdapterPosition()));


            }
        });
    }

    @Override
    public int getItemCount() {
        return sellerList.size();
    }

    interface ItemClickedListener{
        void itemclicked(SellerBo sellerBo);
    }
}