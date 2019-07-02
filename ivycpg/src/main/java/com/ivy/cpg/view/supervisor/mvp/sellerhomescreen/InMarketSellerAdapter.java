package com.ivy.cpg.view.supervisor.mvp.sellerhomescreen;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.ivy.cpg.view.supervisor.chat.StartChatActivity;
import com.ivy.cpg.view.supervisor.mvp.SupervisorActivityHelper;
import com.ivy.cpg.view.supervisor.mvp.models.SellerBo;
import com.ivy.cpg.view.supervisor.mvp.sellerdetailmap.SellerDetailMapActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FontUtils;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

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
        private LinearLayout routeLayout,messageLayout;
        private ImageView userImage;

        public MyViewHolder(View view) {
            super(view);
            userName = view.findViewById(R.id.tv_user_name);
            retailerName = view.findViewById(R.id.tv_address);
            retailerVisit = view.findViewById(R.id.tv_start_time);
            target = view.findViewById(R.id.tv_target_outlet);
            covered = view.findViewById(R.id.tv_outlet_covered);
            userImage = view.findViewById(R.id.usr_img);

            routeLayout = view.findViewById(R.id.route_layout);
            messageLayout = view.findViewById(R.id.message_layout);

            userName.setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.REGULAR));
            retailerName.setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.REGULAR));
            retailerVisit.setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.REGULAR));
            target.setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.REGULAR));
            covered.setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.REGULAR));

            ((TextView) view.findViewById(R.id.tv_message)).setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.REGULAR));
            ((TextView) view.findViewById(R.id.tv_route)).setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.REGULAR));
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
        holder.retailerVisit.setText(context.getResources().getString(R.string.visit_time)+" "+ DateTimeUtils.getTimeFromMillis(sellerArrayList.get(position).getInTime()));
        holder.target.setText(context.getResources().getString(R.string.targeted)+" "+sellerArrayList.get(position).getTarget());
        holder.covered.setText(context.getResources().getString(R.string.covered)+" "+sellerArrayList.get(position).getCovered());

        holder.routeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SellerDetailMapActivity.class);
                intent.putExtra("SellerId", sellerArrayList.get(position).getUserId());
                intent.putExtra("screentitle", sellerArrayList.get(position).getUserName());
                intent.putExtra("Date",sellerMapHomePresenter.getSelectedDate());
                intent.putExtra("UUID",sellerArrayList.get(position).getUid());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });

        holder.messageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (SupervisorActivityHelper.getInstance().isChatConfigAvail(context)) {
                    if (!sellerArrayList.get(holder.getAdapterPosition()).getUid().equals("")) {
                        Intent intent = new Intent(context, StartChatActivity.class);
                        intent.putExtra("UUID", sellerArrayList.get(holder.getAdapterPosition()).getUid());
                        intent.putExtra("name",sellerArrayList.get(holder.getAdapterPosition()).getUserName());
                        context.startActivity(intent);
                    }
                }else{
                    Toast.makeText(context, "No Chat Config Enabled..", Toast.LENGTH_SHORT).show();
                }
            }
        });

        setProfileImage(holder.userImage,
                sellerArrayList.get(position).getImagePath(),
                sellerArrayList.get(position).getUserId());

    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return sellerArrayList.size();
    }

    private void setProfileImage(ImageView userView,String imagePath,int userId) {
        try {
            if (imagePath != null && !"".equals(imagePath)) {
                String[] imgPaths = imagePath.split("/");
                String path = imgPaths[imgPaths.length - 1];
                File imgFile = new File(context.getExternalFilesDir(
                        Environment.DIRECTORY_DOWNLOADS)
                        + "/"
                        + userId
                        + DataMembers.DIGITAL_CONTENT
                        + "/"
                        + DataMembers.USER + "/"
                        + path);
                if (imgFile.exists()) {
                    try {
                        userView.setScaleType(ImageView.ScaleType.FIT_XY);
                        userView.setAdjustViewBounds(true);

                        Glide.with(context)
                                .load(imgFile)
                                .centerCrop()
                                .placeholder(R.drawable.ic_default_user)
                                .error(R.drawable.ic_default_user)
                                .into(userView);

                    } catch (Exception e) {
                        Commons.printException("" + e);
                    }
                } else {
                    userView
                            .setImageResource(R.drawable.ic_default_user);
                }
            }
        }catch(Exception e){
            Commons.printException(e);
        }
    }
}