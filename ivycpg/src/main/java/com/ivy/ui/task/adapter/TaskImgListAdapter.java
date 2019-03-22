package com.ivy.ui.task.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ivy.core.base.view.BaseActivity;
import com.ivy.cpg.view.task.TaskDataBO;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.utils.AppUtils;
import com.ivy.utils.FileUtils;

import java.util.ArrayList;

public class TaskImgListAdapter extends RecyclerView.Adapter<TaskImgListAdapter.TaskImgViewHolder> {

    private ArrayList<TaskDataBO> imgList = new ArrayList<>();
    private Context mContext;
    private boolean isDisplayOnly;
    private PhotoClickListener photoClickListener;

    public TaskImgListAdapter(ArrayList<TaskDataBO> imgList, Context context, boolean isDisplayOnly, PhotoClickListener photoClickListener) {
        this.imgList = imgList;
        this.mContext = context;
        this.isDisplayOnly = isDisplayOnly;
        this.photoClickListener = photoClickListener;
    }

    @NonNull
    @Override
    public TaskImgListAdapter.TaskImgViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_row_image_list, parent, false);

        return new TaskImgViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskImgListAdapter.TaskImgViewHolder holder, int position) {
        if (!isDisplayOnly) {
            if (holder.getAdapterPosition() == 0) {
                holder.deleteImg.setVisibility(View.GONE);
                holder.taskImg.setImageResource(R.drawable.bg_add_photo);
            } else {
                holder.deleteImg.setVisibility(View.VISIBLE);
            }
        }
        String path = FileUtils.photoFolderPath + "/" + imgList.get(holder.getAdapterPosition()).getTaskImg();

        // if (FileUtils.isFileExisting(path)) {
        Uri uri = FileUtils
                .getUriFromFile(mContext.getApplicationContext(), path);

        Glide.with(mContext).load(uri)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .centerCrop()
                .placeholder(R.drawable.bg_add_photo)
                .error(R.drawable.bg_add_photo)
                .into(AppUtils.getRoundedImageTarget(mContext, holder.taskImg, (float) 6));
        //}
        holder.deleteImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImageDeleteAlert(imgList.get(holder.getAdapterPosition()).getTaskImg(), holder.getAdapterPosition());
            }
        });

        holder.taskImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.getAdapterPosition() == 0
                        && !isDisplayOnly)
                    photoClickListener.onTakePhoto();
            }
        });


    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return imgList.size();
    }

    public class TaskImgViewHolder extends RecyclerView.ViewHolder {
        private ImageView taskImg;
        private ImageView deleteImg;

        public TaskImgViewHolder(View itemView) {
            super(itemView);
            taskImg = itemView.findViewById(R.id.task_image_view);
            deleteImg = itemView.findViewById(R.id.delete_image_view);

            if (isDisplayOnly) {
                taskImg.setClickable(false);
                taskImg.setOnClickListener(null);
            }
        }
    }


    public interface PhotoClickListener {

        void onTakePhoto();
    }

    private void showImageDeleteAlert(final String imageNameStarts, int position) {

        ((BaseActivity) mContext).showAlert("", mContext.getString(R.string.do_you_want_to_delete_the_image), new CommonDialog.PositiveClickListener() {
            @Override
            public void onPositiveButtonClick() {
                FileUtils.deleteFiles(FileUtils.photoFolderPath,
                        imageNameStarts);
                imgList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, imgList.size());
            }
        }, new CommonDialog.negativeOnClickListener() {
            @Override
            public void onNegativeButtonClick() {

            }
        });
    }


}
