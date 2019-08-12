package com.ivy.ui.gallery.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.ivy.cpg.view.survey.SignatureView;
import com.ivy.lib.view.SectionedRecyclerViewAdapter;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.util.Commons;
import com.ivy.ui.gallery.model.GalleryBo;
import com.ivy.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import static com.ivy.utils.FileUtils.decodeFile;

public class GalleryViewAdapter extends SectionedRecyclerViewAdapter<RecyclerView.ViewHolder> {
    private final Context mContext;
    private HashMap<String, ArrayList<GalleryBo>> galleryListHashMap;
    private ArrayList<String> sectionArrayList;
    private ItemClickListener animalItemClickListener;
    private Fragment fragment;

    public GalleryViewAdapter(Context context, HashMap<String, ArrayList<GalleryBo>> mGalleryListHashMap, ArrayList<String> mSectionArrayList, ItemClickListener itemClickListener, Fragment mFragment) {
        mContext = context;
        galleryListHashMap = mGalleryListHashMap;
        sectionArrayList = mSectionArrayList;
        animalItemClickListener = itemClickListener;
        fragment = mFragment;
    }

    @Override
    public int getSectionCount() {
        return sectionArrayList.size();
    }

    @Override
    public int getItemCount(int section) {
        return galleryListHashMap.get(sectionArrayList.get(section)).size();
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, boolean header) {
        View v = null;
        if (header) {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_section, parent, false);
            return new SectionViewHolder(v);
        } else {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.gallery_list_item, parent, false);
            return new ItemViewHolder(v);
        }
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int sectionPos) {
        SectionViewHolder sectionViewHolder = (SectionViewHolder) holder;
        String sectionName = sectionArrayList.get(sectionPos);
        sectionViewHolder.sectionNameTv.setText(sectionName);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int section, int relativePosition, int absolutePosition) {
        ItemViewHolder itemViewHolder = (ItemViewHolder) holder;


        GalleryBo galleryBo = galleryListHashMap
                .get(sectionArrayList.get(section))
                .get(relativePosition);

        Uri uri = FileUtils
                .getUriFromFile(mContext.getApplicationContext(), galleryBo.getFilePath());

        // Load the image with Glide to prevent OOM error when the image drawables are very large.
        Glide.with(mContext).load(uri)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .centerCrop()
                .listener(new RequestListener<Uri, Bitmap>() {
                    @Override
                    public boolean onException(Exception e, Uri model, Target<Bitmap> target, boolean isFirstResource) {

                        // fragment.startPostponedEnterTransition();

                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Uri model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        // fragment.startPostponedEnterTransition();
                        return false;

                    }
                })
                .placeholder(R.drawable.no_photo)
                .error(R.drawable.no_photo)
                .into(itemViewHolder.imageView);

        //  ViewCompat.setTransitionName(itemViewHolder.imageView, galleryBo.getFilePath());

        itemViewHolder.productNameTv.setText(galleryBo.getImageName());

        itemViewHolder.itemView.setOnClickListener(v -> {
            animalItemClickListener.onItemClicked(itemViewHolder.imageView, holder.getAdapterPosition(), relativePosition, galleryListHashMap.get(sectionArrayList.get(section)));
        });
    }


    // SectionViewHolder Class for Sections
    public class SectionViewHolder extends RecyclerView.ViewHolder {

        final TextView sectionNameTv;

        public SectionViewHolder(View itemView) {
            super(itemView);
            sectionNameTv = itemView.findViewById(R.id.source);
        }
    }

    // ItemViewHolder Class for Items in each Section
    public class ItemViewHolder extends RecyclerView.ViewHolder {

        final AppCompatTextView productNameTv;
        final AppCompatImageView imageView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.itemImage);
            productNameTv = itemView.findViewById(R.id.itemTitle);
            DisplayMetrics displaymetrics = new DisplayMetrics();
            ((AppCompatActivity) mContext).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            imageView.getLayoutParams().height = displaymetrics.heightPixels / 4;

        }
    }


    /**
     * A listener that is attached to all ViewHolders to handle image loading events and clicks.
     */
    public interface ItemClickListener {

        void onItemClicked(ImageView view, int adapterPosition, int imageViewPos, ArrayList<GalleryBo> galleryBoArrayList);
    }
}
