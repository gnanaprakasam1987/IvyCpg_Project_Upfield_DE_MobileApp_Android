package com.ivy.cpg.view.supervisor.mvp.sellerperformance.sellerperformancelist;

import android.content.Context;
import android.os.Environment;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ivy.cpg.view.supervisor.mvp.models.SellerBo;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.FontUtils;

import java.io.File;
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
        private ImageView userImage;

        public MyViewHolder(View view) {
            super(view);
            sellerNameTv = view.findViewById(R.id.seller_name);
            sellerPositionTv = view.findViewById(R.id.seller_position);
            sellerPerformancePercentTv = view.findViewById(R.id.seller_perform_percent);
            progressBar = view.findViewById(R.id.progressBar);
            userImage = view.findViewById(R.id.user_image);

            sellerNameTv.setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.REGULAR));
            sellerPositionTv.setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.REGULAR));
            sellerPerformancePercentTv.setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.MEDIUM));
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

        holder.sellerPerformancePercentTv.setText(sellerProductive>100?"100%":sellerProductive+"%");
        holder.progressBar.setProgress(sellerProductive);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                itemClickedListener.itemclicked(sellerList.get(holder.getAdapterPosition()));


            }
        });

        setProfileImage(holder.userImage,
                sellerList.get(holder.getAdapterPosition()).getImagePath(),
                sellerList.get(holder.getAdapterPosition()).getUserId());
    }

    @Override
    public int getItemCount() {
        return sellerList.size();
    }

    interface ItemClickedListener{
        void itemclicked(SellerBo sellerBo);
    }

    private void setProfileImage(ImageView userView, String imagePath, int userId) {
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