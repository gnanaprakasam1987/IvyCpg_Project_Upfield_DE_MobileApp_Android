package com.ivy.cpg.view.supervisor.mvp.sellerlistview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.ivy.cpg.view.supervisor.chat.StartChatActivity;
import com.ivy.cpg.view.supervisor.customviews.tooltip.Tooltip;
import com.ivy.cpg.view.supervisor.mvp.SupervisorActivityHelper;
import com.ivy.cpg.view.supervisor.mvp.models.SellerBo;
import com.ivy.cpg.view.supervisor.mvp.sellerdetailmap.SellerDetailMapActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FontUtils;

import java.io.File;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class SellerListAdapter extends RecyclerView.Adapter<SellerListAdapter.MyViewHolder> {

    private Context context;
    private boolean showStatus;
    private ArrayList<SellerBo> sellerListBos = new ArrayList<>();
    private String selectedDate;

    SellerListAdapter(Context context,boolean showStatus,ArrayList<SellerBo> sellerListBos,String selectedDate){
        this.context = context;
        this.showStatus = showStatus;
        this.sellerListBos = sellerListBos;
        this.selectedDate = selectedDate;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView userName,routeText,statusTextview,performancePercent,outletCoveredTxt,messageText;
        private RelativeLayout statusLayout;
        private ImageView infoIconImg,userImage;
        private ProgressBar progressBar;
        private LinearLayout routeLayout,messageLayout;

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
            routeLayout = view.findViewById(R.id.route_layout);
            userImage = view.findViewById(R.id.user_img);
            messageLayout = view.findViewById(R.id.message_layout);

            userName.setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.REGULAR));
            routeText.setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.REGULAR));
            statusTextview.setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.REGULAR));
            performancePercent.setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.REGULAR));
            outletCoveredTxt.setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.REGULAR));
            messageText.setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.REGULAR));

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
        if (sellerListBos.get(holder.getAdapterPosition()).getTarget()!=0) {
            sellerProductive = (int)((float)sellerListBos.get(holder.getAdapterPosition()).getCovered() / (float)sellerListBos.get(holder.getAdapterPosition()).getTarget() * 100);
        }

        holder.performancePercent.setText(sellerProductive>100?"100%":sellerProductive+"%");

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
                            .setText("Last visit at " + DateTimeUtils.getTimeFromMillis(sellerListBos.get(holder.getAdapterPosition()).getInTime()))
                            .setTextSize(R.dimen._11sdp)
                            .setBackgroundColor(ContextCompat.getColor(context, R.color.tootl_tip_bg))
                            .setTextColor(ContextCompat.getColor(context, R.color.white))
                            .setPadding(10f);
                    builder.show();
                }
            });
        }else{
            holder.infoIconImg.setVisibility(View.GONE);
        }

        holder.routeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, SellerDetailMapActivity.class);
                intent.putExtra("SellerId", sellerListBos.get(holder.getAdapterPosition()).getUserId());
                intent.putExtra("screentitle", sellerListBos.get(holder.getAdapterPosition()).getUserName() );
                intent.putExtra("Date",selectedDate);
                intent.putExtra("UUID",sellerListBos.get(holder.getAdapterPosition()).getUid());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });

        holder.messageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (SupervisorActivityHelper.getInstance().isChatConfigAvail(context)) {
                    if (!sellerListBos.get(holder.getAdapterPosition()).getUid().equals("")) {
                        Intent intent = new Intent(context, StartChatActivity.class);
                        intent.putExtra("UUID", sellerListBos.get(holder.getAdapterPosition()).getUid());
                        intent.putExtra("name",sellerListBos.get(holder.getAdapterPosition()).getUserName());
                        context.startActivity(intent);
                    } else
                        Toast.makeText(context, "No Chat Found..", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(context, "No Chat Config Enabled..", Toast.LENGTH_SHORT).show();
                }
            }
        });

        setProfileImage(holder.userImage,
                sellerListBos.get(holder.getAdapterPosition()).getImagePath(),
                sellerListBos.get(holder.getAdapterPosition()).getUserId());
    }

    @Override
    public int getItemCount() {
        return sellerListBos.size();
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
                        //  profileImageView.setImageBitmap(getCircularBitmapFrom(myBitmap));

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

