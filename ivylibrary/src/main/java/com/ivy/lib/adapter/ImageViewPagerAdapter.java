package com.ivy.lib.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ivy.lib.R;

import java.util.ArrayList;

public class ImageViewPagerAdapter extends PagerAdapter {
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private ArrayList<String> imgList;
    private String filePath;
    private int selectedPosition;

    public ImageViewPagerAdapter(Context mContext, ArrayList<String> imgList, String filePath, int selectedPosition) {
        this.mContext = mContext;
        this.mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.imgList = imgList;
        this.filePath = filePath;
        this.selectedPosition = selectedPosition;
    }

    @Override
    public int getCount() {
        return imgList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        // The object returned by instantiateItem() is a key/identifier. This method checks whether
        // the View passed to it (representing the page) is associated with that key or not.
        // It is required by a PagerAdapter to function properly.
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = mLayoutInflater.inflate(R.layout.image_view_pager_layout, container, false);

        String path;
        if (selectedPosition == -1)
            path = filePath + imgList.get(position);
        else {
            path = filePath + imgList.get(selectedPosition);
            selectedPosition = -1;
        }

        Uri uri = Uri.parse("file://" + path);

        AppCompatImageView imageView = itemView.findViewById(R.id.img_view);

        Glide.with(mContext).load(uri)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .centerCrop()
                .placeholder(R.drawable.no_image_available)
                .error(R.drawable.no_image_available)
                .into((imageView));

        container.addView(itemView);

        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        // Removes the page from the container for the given position. We simply removed object using removeView()
        // but could’ve also used removeViewAt() by passing it the position.
        try {
            // Remove the view from the container
            container.removeView((View) object);

            // Try to clear resources used for displaying this view

            View v = ((View) object).findViewById(R.id.img_view);
            Glide.clear(v);

            // Remove any resources used by this view
            unbindDrawables((View) object);
            // Invalidate the object
        } catch (Exception e) {
        }
    }

    /**
     * Recursively unbind any resources from the provided view. This method will clear the resources of all the
     * children of the view before invalidating the provided view itself.
     *
     * @param view The view for which to unbind resource.
     */
    protected void unbindDrawables(View view) {
        if (view.getBackground() != null) {
            view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            ((ViewGroup) view).removeAllViews();
        }
    }

}
