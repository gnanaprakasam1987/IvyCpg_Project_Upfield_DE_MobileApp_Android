package com.ivy.cpg.view.supervisor.mvp.supervisorhomepage;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ivy.cpg.view.supervisor.mvp.sellermapview.SellerMapViewActivity;
import com.ivy.sd.png.asean.view.R;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private Context context;

    MyAdapter(Context context){
        this.context = context;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView userName;
        private LinearLayout routeLayout;

        public MyViewHolder(View view) {
            super(view);
            userName = view.findViewById(R.id.tv_user_name);
            routeLayout = view.findViewById(R.id.route_layout);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.map_seller_info_layout, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {

        final View itemView = holder.itemView;

        holder.userName.setText("Test "+position);

        holder.routeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SellerMapViewActivity.class);
                intent.putExtra("SellerId", "1695");
                intent.putExtra("screentitle", "Seller");
                intent.putExtra("TrackingType", 1);
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
        return 4;
    }
}