package com.ivy.cpg.view.supervisor.mvp.sellermapview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ivy.cpg.view.supervisor.customviews.tooltip.Tooltip;
import com.ivy.cpg.view.supervisor.mvp.SupervisorModelBo;
import com.ivy.sd.png.asean.view.R;
import com.ivy.utils.FontUtils;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class SellerListAdapter extends RecyclerView.Adapter<SellerListAdapter.MyViewHolder> {

    private Context context;
    private boolean showStatus;
    private ArrayList<SupervisorModelBo> sellerListBos = new ArrayList<>();

    SellerListAdapter(Context context,boolean showStatus,ArrayList<SupervisorModelBo> sellerListBos){
        this.context = context;
        this.showStatus = showStatus;
        this.sellerListBos = sellerListBos;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView userName,routeText,statusTextview,performancePercent,outletCoveredTxt,messageText;
        private RelativeLayout statusLayout;
        private ImageView infoIconImg;
        private ProgressBar progressBar;

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
            progressBar = view.findViewById(R.id.progress_bar);

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

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        holder.userName.setText(sellerListBos.get(holder.getAdapterPosition()).getUserName());
        holder.outletCoveredTxt.setText("Target/Covered : "+sellerListBos.get(holder.getAdapterPosition()).getTarget()+
                "/"+sellerListBos.get(holder.getAdapterPosition()).getCovered());

        int sellerProductive = 0;
        if (sellerListBos.get(holder.getAdapterPosition()).getCovered()!=0) {
            sellerProductive = (int)((float)sellerListBos.get(holder.getAdapterPosition()).getCovered() / (float)sellerListBos.get(holder.getAdapterPosition()).getTarget() * 100);
        }

        holder.performancePercent.setText(sellerProductive+"%");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            holder.progressBar.setProgress(sellerProductive,true);
        else
            holder.progressBar.setProgress(sellerProductive);


        if(!showStatus)
            holder.statusLayout.setVisibility(View.GONE);
        else{
            if(sellerListBos.get(position).isAttendanceDone()){
                holder.statusTextview.setText("In Market");
                holder.statusLayout.setBackgroundDrawable(ContextCompat.getDrawable(context,R.drawable.covered_bg_gradient));
            }else{
                holder.statusTextview.setText("   Absent   ");
                holder.statusLayout.setBackgroundDrawable(ContextCompat.getDrawable(context,R.drawable.absent_seller_text_bg_gradient));
            }
        }

        if(sellerListBos.get(position).isAttendanceDone()) {
            holder.statusLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Tooltip.Builder builder = new Tooltip.Builder(holder.infoIconImg, R.style.Tooltip)
                            .setCancelable(true)
                            .setDismissOnClick(false)
                            .setCornerRadius(5f)
                            .setGravity(Gravity.BOTTOM)
                            .setText("Last visit at " + convertTime(sellerListBos.get(holder.getAdapterPosition()).getTimeIn()))
                            .setTextSize(R.dimen._11sdp)
                            .setBackgroundColor(ContextCompat.getColor(context, R.color.tootl_tip_bg))
                            .setTextColor(ContextCompat.getColor(context, R.color.WHITE))
                            .setPadding(10f);
                    builder.show();
                }
            });
        }else{
            holder.infoIconImg.setVisibility(View.GONE);
        }

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
        return sellerListBos.size();
    }

    public String convertTime(Long time){

        if(time !=null && time != 0) {
            Date date = new Date(time);
            Format format = new SimpleDateFormat("hh:mm a", Locale.US);
            return format.format(date);
        }else
            return "";
    }
}

