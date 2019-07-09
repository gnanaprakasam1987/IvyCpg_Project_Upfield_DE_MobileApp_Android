package com.ivy.cpg.view.planogram;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.viewpager.widget.PagerAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.ivy.sd.png.asean.view.BuildConfig;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.util.Commons;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * Custom pager adapter which will manually create the pages needed for showing an slide pages gallery.
 *
 */
public class GalleryAdapter extends PagerAdapter {

    private static final String TAG = "GalleryAdapter";

    private final List<String> mItems;
    private final LayoutInflater mLayoutInflater;
    private Context context;
    private String photoNamePath;
    /**
     * The click event listener which will propagate click events to the parent or any other listener set
     */
    private PlanoGramFragment.ItemClickSupport mOnItemClickListener;

    /**
     * Constructor for gallery adapter which will create and screen slide of images.
     *
     * @param context
     *         The context which will be used to inflate the layout for each page.
     * @param mediaGallery
     *         The list of items which need to be displayed as screen slide.
     */
    GalleryAdapter(@NonNull Context context,
                          @NonNull ArrayList<String> mediaGallery,String photoNamePath) {
        super();

        // Inflater which will be used for creating all the necessary pages
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
        this.photoNamePath = photoNamePath;

        // The items which will be displayed.
        mItems = mediaGallery;
    }

    @Override
    public int getCount() {
        // Just to be safe, check also if we have an valid list of items - never return invalid size.
//        return null == mItems ? 0 : mItems.size();
        return mItems.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        // The object returned by instantiateItem() is a key/identifier. This method checks whether
        // the View passed to it (representing the page) is associated with that key or not.
        // It is required by a PagerAdapter to function properly.
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
        // This method should create the page for the given position passed to it as an argument.
        // In our case, we inflate() our layout resource to create the hierarchy of view objects and then
        // set resource for the ImageView in it.
        // Finally, the inflated view is added to the container (which should be the ViewPager) and return it as well.

        // inflate our layout resource
        View itemView = mLayoutInflater.inflate(R.layout.image_pager_layout, container, false);
        ImageView displayImg = (ImageView) itemView.findViewById(R.id.image_item);

        if(mItems.size() > 0) {
            // Display the resource on the view
            displayGalleryItem(displayImg, mItems.get(position));
        }

        // Add our inflated view to the container
        container.addView(itemView);

        // Detect the click events and pass them to any listeners
        displayImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mOnItemClickListener) {
                    mOnItemClickListener.onItemClicked(photoNamePath+mItems.get(position),false);
                }
            }
        });

        displayImg.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (null != mOnItemClickListener) {
                    mOnItemClickListener.onItemClicked(mItems.get(position),true);
                }
                return false;
            }
        });

        // Return our view
        return itemView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        // Removes the page from the container for the given position. We simply removed object using removeView()
        // but could’ve also used removeViewAt() by passing it the position.
        try {
            // Remove the view from the container
            container.removeView((View) object);

            // Try to clear resources used for displaying this view

            View v = ((View) object).findViewById(R.id.image_item);
            Glide.clear(v);

            // Remove any resources used by this view
            unbindDrawables((View) object);
            // Invalidate the object
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    /**
     * Recursively unbind any resources from the provided view. This method will clear the resources of all the
     * children of the view before invalidating the provided view itself.
     *
     * @param view
     *         The view for which to unbind resource.
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

    /**
     * Set an listener which will notify of any click events that are detected on the pages of the view pager.
     *
     * @param onItemClickListener
     *         The listener. If {@code null} it will disable any events from being sent.
     */
    public void setOnItemClickListener(PlanoGramFragment.ItemClickSupport onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    /**
     * Display the gallery image into the image view provided.
     *
     * @param galleryView
     *         The view which will display the image.
     * @param galleryItem
     *         The item from which to get the image.
     */
    private void displayGalleryItem(ImageView galleryView, String galleryItem) {
        if (null != galleryItem && galleryView != null) {
            String path = photoNamePath+galleryItem;
            if (isImagePresent(path)) {
                Uri uri = getUriFromFile(path);
                galleryView.setImageURI(uri);
            }
        }
    }

    /**
     * To check file availability
     *
     * @param path File path
     * @return Availability
     */
    private boolean isImagePresent(String path) {
        File f = new File(path);
        return f.exists();
    }

    /**
     * Getting file URI
     *
     * @param path File path
     * @return URI
     */
    private Uri getUriFromFile(String path) {
        File f = new File(path);
        if (Build.VERSION.SDK_INT >= 24) {
            return FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", f);

        } else {
            return Uri.fromFile(f);
        }

    }
}