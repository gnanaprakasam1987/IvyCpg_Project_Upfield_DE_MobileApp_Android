package com.ivy.lib.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ivy.lib.ImageAdapterListener;
import com.ivy.lib.R;
import com.ivy.lib.Utils;
import com.ivy.lib.view.ImageViewActivity;

import java.util.ArrayList;

public class GridImageViewAdapter extends RecyclerView.Adapter<GridImageViewAdapter.ImageViewHolder> {
    private Context mContext;
    private ArrayList<String> imageList;
    private String filePath;
    private ImageAdapterListener imageAdapterListener;

    public GridImageViewAdapter(Context mContext, ArrayList<String> imageList, String filePath) {
        this.mContext = mContext;
        this.imageList = imageList;
        this.filePath = filePath;
    }

    public GridImageViewAdapter(Context mContext, ArrayList<String> imageList, String filePath, ImageAdapterListener imageAdapterListener) {
        this.mContext = mContext;
        this.imageList = imageList;
        this.filePath = filePath;
        this.imageAdapterListener = imageAdapterListener;
    }


    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_image_list, parent, false);

        return new ImageViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ImageViewHolder holder, int position) {

        String imageName = imageList.get(position);

        if (imageAdapterListener != null
                && holder.getAdapterPosition() == 0) {
            holder.deleteImg.setVisibility(View.GONE);
            holder.imageView.setImageResource(R.drawable.bg_add_photo);
            holder.fileNameTv.setVisibility(View.GONE);
        } else {
            if (imageAdapterListener != null)
                holder.deleteImg.setVisibility(View.VISIBLE);

            String path = filePath + imageName;

            Uri uri = Uri.parse("file://" + path);


            Glide.with(mContext).load(uri)
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .centerCrop()
                    .placeholder(R.drawable.no_image_available)
                    .error(R.drawable.no_image_available)
                    .into(Utils.getRoundedImageTarget(mContext, holder.imageView, (float) 6));

            holder.fileNameTv.setVisibility(View.VISIBLE);
            holder.fileNameTv.setText(imageName);

        }





        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageAdapterListener != null
                        && holder.getAdapterPosition() == 0)
                    imageAdapterListener.onTakePhoto();
                else {
                    if (imageList.get(holder.getAdapterPosition()) != null
                            && !imageList.get(holder.getAdapterPosition()).isEmpty()) {
                        Intent viewIntent = new Intent(mContext, ImageViewActivity.class);
                        viewIntent.putExtra("FilePath", filePath);
                        viewIntent.putStringArrayListExtra("imageName", imageList);
                        viewIntent.putExtra("selectedPos", holder.getAdapterPosition());
                        mContext.startActivity(viewIntent);
                    } else
                        Toast.makeText(mContext, mContext.getString(R.string.image_not_available), Toast.LENGTH_LONG).show();
                }
            }
        });

        holder.deleteImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageAdapterListener.deletePhoto(imageList.get(holder.getAdapterPosition()));
            }
        });


    }

    @Override
    public int getItemCount() {
            return imageList.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {
        private AppCompatImageView imageView;
        private AppCompatImageView deleteImg;
        private AppCompatTextView fileNameTv;

        public ImageViewHolder(View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.image_view);
            fileNameTv = itemView.findViewById(R.id.file_name_tv);
            deleteImg = itemView.findViewById(R.id.delete_image_view);


            DisplayMetrics displaymetrics = new DisplayMetrics();
            ((AppCompatActivity) mContext).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            imageView.getLayoutParams().height = displaymetrics.heightPixels / 4;
        }
    }


}
