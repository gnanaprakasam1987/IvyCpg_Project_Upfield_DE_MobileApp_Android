package com.ivy.ui.photocapture.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.ivy.core.base.view.BaseActivity;
import com.ivy.cpg.view.photocapture.PhotoCaptureLocationBO;
import com.ivy.sd.png.asean.view.BuildConfig;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.ui.photocapture.adapter.PhotoGalleryAdapter;
import com.ivy.ui.photocapture.adapter.PhotoGridAdapter;
import com.ivy.utils.FontUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PhotoGalleryActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar mToolBar;

    @BindView(R.id.tv_toolbar_title)
    TextView mToolBarTitleTxt;

    @BindView(R.id.gal_recycler_view)
    RecyclerView mRecyclerView;

    private HashMap<String, PhotoCaptureLocationBO> photoCaptureMap;

    private ArrayList<String> selectedItemsList = new ArrayList<>();

    private LinkedHashMap<String, ArrayList<PhotoCaptureLocationBO>> photoAdapterMap;

    @Override
    public int getLayoutId() {
        return R.layout.gallery;
    }

    @Override
    protected void initVariables() {
        photoAdapterMap = new LinkedHashMap<>();
    }

    @Override
    public void initializeDi() {

    }

    @Override
    protected void getMessageFromAliens() {
        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().getBoolean("isFromPhotoCapture", true))
                photoCaptureMap = (HashMap<String, PhotoCaptureLocationBO>) getIntent().getExtras().get("data");
        }
    }

    @Override
    protected void setUpViews() {
        setUnBinder(ButterKnife.bind(this));
        setUpToolbar();
        setUpRecyclerView();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_gallery, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            finish();
            return true;
        } else if (i == R.id.menu_gallery_share) {
            shareImages();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpToolbar() {
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mToolBarTitleTxt.setTypeface(FontUtils.getFontBalooHai(this, FontUtils.FontType.REGULAR));
        mToolBarTitleTxt.setText(getResources().getString(R.string.my_gallery));
    }

    private void setUpRecyclerView() {
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setItemViewCacheSize(200);
        mRecyclerView.setDrawingCacheEnabled(true);
        mRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        processData();
    }

    //Update the screen title based on the selection of images
    @SuppressLint("SetTextI18n")
    protected void setSelectionScreenTitle(int count) {
        if (count > 0) {
            mToolBarTitleTxt.setText(count + " selected");
        } else {
            mToolBarTitleTxt.setText(getResources().getString(R.string.my_gallery));
        }
        invalidateOptionsMenu();
    }

    private void processData() {
        photoAdapterMap.clear();
        assert photoCaptureMap != null;
        for (Map.Entry<String, PhotoCaptureLocationBO> entry : photoCaptureMap.entrySet()) {
            String productId = entry.getValue().getProductName();

            if (!photoAdapterMap.containsKey(productId))
                photoAdapterMap.put(productId, new ArrayList<PhotoCaptureLocationBO>());
            photoAdapterMap.get(productId).add(entry.getValue());
        }

        PhotoGalleryAdapter photoGalleryAdapter = new PhotoGalleryAdapter(this, photoAdapterMap, new PhotoGridAdapter.PhotoClickListener() {
            @Override
            public void onPhotoClicked(String selectedItem) {
                if (!selectedItemsList.contains(selectedItem))
                    selectedItemsList.add(selectedItem);

                setSelectionScreenTitle(selectedItemsList.size());
            }

            @Override
            public void onPhotoClickRemoved(String selectedItem) {
                if (selectedItemsList.contains(selectedItem))
                    selectedItemsList.remove(selectedItem);

                setSelectionScreenTitle(selectedItemsList.size());
            }
        });
        mRecyclerView.setAdapter(photoGalleryAdapter);


    }


    private void shareImages() {
        if (selectedItemsList.size() > 0) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND_MULTIPLE);
            intent.putExtra(Intent.EXTRA_SUBJECT, "Pictures");
            intent.setType("image/*");
            ArrayList<Uri> files = new ArrayList<>();
            /* List of the files you want to send */
            for (String path : selectedItemsList) {
                File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/"
                        + DataMembers.photoFolderName + photoCaptureMap.get(path).getImageName());
                if (Build.VERSION.SDK_INT >= 24) {
                    files.add(FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", file));

                } else {
                    files.add(Uri.fromFile(file));
                }

            }
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
            startActivity(Intent.createChooser(intent, "Share Image"));
        }
    }
}
