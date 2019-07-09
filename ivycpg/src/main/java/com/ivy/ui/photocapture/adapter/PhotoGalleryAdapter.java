package com.ivy.ui.photocapture.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ivy.ui.photocapture.model.PhotoCaptureLocationBO;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.MyGridView;
import com.ivy.utils.FontUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class PhotoGalleryAdapter extends RecyclerView.Adapter<PhotoGalleryAdapter.PhotoGalleryViewHolder> {


    private final LinkedHashMap<String, ArrayList<PhotoCaptureLocationBO>> photoGalleryData;

    private Context mContext;

    private ArrayList<String> keyList = new ArrayList<>();

    private PhotoGridAdapter.PhotoClickListener mPhotoClickListener;

    public PhotoGalleryAdapter(Context context, LinkedHashMap<String, ArrayList<PhotoCaptureLocationBO>> photoGalleryData,PhotoGridAdapter.PhotoClickListener photoClickListener) {
        this.photoGalleryData = photoGalleryData;
        this.mContext = context;
        this.mPhotoClickListener =photoClickListener;
        for (String key : photoGalleryData.keySet()) {
            ArrayList<PhotoCaptureLocationBO> value = photoGalleryData.get(key);
            keyList.add(value.get(0).getProductName());
        }

    }

    @Override
    public PhotoGalleryAdapter.PhotoGalleryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.gal_rec_item_lay, parent, false);

        return new PhotoGalleryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(PhotoGalleryAdapter.PhotoGalleryViewHolder holder, int position) {
        holder.ProdName.setText(keyList.get(position));
        holder.PhoneCaptureGrid.setAdapter(new PhotoGridAdapter(mContext, photoGalleryData.get(keyList.get(position)),mPhotoClickListener));
        holder.ProdName.setTypeface(FontUtils.getFontRoboto(mContext, FontUtils.FontType.MEDIUM));
    }

    @Override
    public int getItemCount() {
        return photoGalleryData.size();
    }


    class PhotoGalleryViewHolder extends RecyclerView.ViewHolder {
        private TextView ProdName;
        private MyGridView PhoneCaptureGrid;

        private PhotoGalleryViewHolder(View view) {
            super(view);
            ProdName = (TextView) view.findViewById(R.id.ProdName);
            PhoneCaptureGrid = (MyGridView) view.findViewById(R.id.PhoneCaptureGrid);

        }
    }

}
