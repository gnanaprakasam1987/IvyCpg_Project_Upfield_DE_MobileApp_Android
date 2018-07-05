package com.ivy.cpg.view.supervisor.mvp.sellerperformance;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ivy.cpg.view.supervisor.utils.FontUtils;
import com.ivy.sd.png.asean.view.R;

public class SellerPerformanceListAdapter extends RecyclerView.Adapter<SellerPerformanceListAdapter.MyViewHolder> {

    private Context context;

    SellerPerformanceListAdapter(Context context){
        this.context = context;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView sellerNameTv,sellerPositionTv,sellerPerformancePercentTv;

        public MyViewHolder(View view) {
            super(view);
            sellerNameTv = view.findViewById(R.id.seller_name);
            sellerPositionTv = view.findViewById(R.id.seller_position);
            sellerPerformancePercentTv = view.findViewById(R.id.seller_perform_percent);

            sellerNameTv.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,context));
            sellerNameTv.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,context));
            sellerNameTv.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM,context));
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
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context,SellerPerformanceDetailActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);

            }
        });
    }

    @Override
    public int getItemCount() {
//            return detailsBos.size();
        return 15;
    }
}