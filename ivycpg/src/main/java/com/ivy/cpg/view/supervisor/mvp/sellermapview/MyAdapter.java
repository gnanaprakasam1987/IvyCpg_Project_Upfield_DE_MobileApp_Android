package com.ivy.cpg.view.supervisor.mvp.sellermapview;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ivy.cpg.view.supervisor.helper.DetailsBo;
import com.ivy.cpg.view.supervisor.customviews.tooltip.Tooltip;
import com.ivy.cpg.view.supervisor.utils.FontUtils;
import com.ivy.sd.png.asean.view.R;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private Context context;
    private boolean showStatus;
    private ArrayList<DetailsBo> detailsBos = new ArrayList<>();

    MyAdapter(Context context,boolean showStatus,ArrayList<DetailsBo> detailsBos){
        this.context = context;
        this.showStatus = showStatus;
        this.detailsBos = detailsBos;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView userName,routeText,statusTextview,performancePercent,outletCoveredTxt,messageText;
        private RelativeLayout statusLayout;
        private ImageView infoIconImg;

        public MyViewHolder(View view) {
            super(view);
            userName = view.findViewById(R.id.tv_user_name);
            routeText = view.findViewById(R.id.tv_route);
            statusTextview = view.findViewById(R.id.tv_status);
            statusLayout = view.findViewById(R.id.status_layout);
            performancePercent = view.findViewById(R.id.tv_percent_txt);
            outletCoveredTxt = view.findViewById(R.id.tv_outlet_covered);
            messageText = view.findViewById(R.id.tv_message);
            infoIconImg = view.findViewById(R.id.info_icon);

            userName.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,context));
            routeText.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,context));
            statusTextview.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,context));
            performancePercent.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,context));
            outletCoveredTxt.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,context));
            messageText.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,context));

        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.seller_recycler_item_, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

//            holder.userName.setText(detailsBos.get(position).getUserName());

        if(!showStatus)
            holder.statusLayout.setVisibility(View.GONE);

//            if(detailsBos.get(position).getStatus().equalsIgnoreCase("In Market")){
//                holder.statusTextview.setText(detailsBos.get(position).getStatus());
//                holder.statusLayout.setBackgroundDrawable(ContextCompat.getDrawable(getContext(),R.drawable.covered_bg_gradient));
//            }else{
//                holder.statusTextview.setText(detailsBos.get(position).getStatus());
//                holder.statusLayout.setBackgroundDrawable(ContextCompat.getDrawable(getContext(),R.drawable.absent_seller_text_bg_gradient));
//            }

        holder.statusLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Tooltip.Builder builder = new Tooltip.Builder(holder.infoIconImg, R.style.Tooltip)
                        .setCancelable(true)
                        .setDismissOnClick(false)
                        .setCornerRadius(5f)
                        .setGravity(Gravity.BOTTOM)
                        .setText("Starts at 10:30 AM")
                        .setTextSize(R.dimen.dimen_12dp)
                        .setBackgroundColor(ContextCompat.getColor(context,R.color.tootl_tip_bg))
                        .setTextColor(ContextCompat.getColor(context,R.color.WHITE))
                        .setPadding(10f);
                builder.show();
            }
        });



        holder.routeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, SellerMapViewActivity.class);
                intent.putExtra("SellerId", 1695);
                intent.putExtra("screentitle", "VSR01" );
                intent.putExtra("TrackingType", 2);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
//            return detailsBos.size();
        return 10;
    }
}

