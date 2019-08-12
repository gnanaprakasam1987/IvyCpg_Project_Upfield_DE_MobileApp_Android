package com.ivy.ui.gallery.view;

import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.ivy.sd.png.asean.view.R;
import com.ivy.ui.gallery.model.GalleryBo;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FileUtils;

import java.util.Date;

public class ImagePagerFragment extends Fragment {

    private static final String KEY_IMAGE_PATH = "com.ivy.ui.gallery.view.key.imagePath";

    public static ImagePagerFragment newInstance(GalleryBo galleryBo) {
        ImagePagerFragment fragment = new ImagePagerFragment();
        Bundle argument = new Bundle();
        argument.putParcelable(KEY_IMAGE_PATH, galleryBo);
        fragment.setArguments(argument);
        return fragment;
    }

    private BottomSheetBehavior bottomSheetBehavior;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image, container, false);

        AppCompatImageView image_view_pager = view.findViewById(R.id.image_view_pager);
        bottomSheetBehavior = BottomSheetBehavior.from(view.findViewById(R.id.imgDetailBottomSheetLayout));
        Bundle arguments = getArguments();
        GalleryBo galleryBo = arguments.getParcelable(KEY_IMAGE_PATH);

        Uri uri = FileUtils
                .getUriFromFile(this.getContext(), galleryBo.getFilePath());
        Glide.with(getActivity())
                .load(uri)
                .asBitmap()
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .centerCrop().into(image_view_pager);

        setHasOptionsMenu(true);
        setUpBottomSheet(view, galleryBo);
        image_view_pager.setOnClickListener(v -> hideBottomSheet());

        return view;
    }

    private void setUpBottomSheet(View view, GalleryBo galleryBo) {

        ((AppCompatTextView) view.findViewById(R.id.file_path_tv)).setText(galleryBo.getImageName());
        ((AppCompatTextView) view.findViewById(R.id.date_tv)).setText(DateTimeUtils.convertDateObjectToRequestedFormat(new Date(galleryBo.getDate()), "dd/MMM/YYYY"));
        ((AppCompatTextView) view.findViewById(R.id.ret_name_tv)).setText(galleryBo.getRetailerName());
        ((AppCompatTextView) view.findViewById(R.id.prod_name_tv)).setText(galleryBo.getName());

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                /*if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                } else if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                } else if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                } else if (newState == BottomSheetBehavior.STATE_SETTLING) {
                }*/
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });

    }

    private void showBottomSheet() {
        bottomSheetBehavior.setHideable(false);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private void hideBottomSheet() {
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_gallery, menu);
    }


    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        menu.findItem(R.id.menu_gallery_delete).setVisible(false);
        menu.findItem(R.id.menu_gallery_share).setVisible(false);
        menu.findItem(R.id.menu_gallery_detail).setVisible(true);
        menu.findItem(R.id.menu_gallery_detail).getIcon().setColorFilter(ContextCompat.getColor(getActivity(), R.color.white), PorterDuff.Mode.SRC_ATOP);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            getActivity().finish();
        } else if (id == R.id.menu_gallery_detail) {
            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                hideBottomSheet();
            } else {
                showBottomSheet();
            }
        }
        return super.onOptionsItemSelected(item);
    }

}
