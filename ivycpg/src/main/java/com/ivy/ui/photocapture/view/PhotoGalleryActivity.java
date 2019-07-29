package com.ivy.ui.photocapture.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseActivity;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.ui.photocapture.di.DaggerPhotoGalleryComponent;
import com.ivy.ui.photocapture.model.PhotoCaptureLocationBO;
import com.ivy.sd.png.asean.view.BuildConfig;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.ui.photocapture.adapter.PhotoGalleryAdapter;
import com.ivy.ui.photocapture.adapter.PhotoGridAdapter;
import com.ivy.ui.photocapture.di.PhotoGalleryModule;
import com.ivy.utils.FileUtils;
import com.ivy.utils.FontUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PhotoGalleryActivity extends BaseActivity {

    @Inject
    BaseIvyPresenter<BaseIvyView> presenter;

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
        DaggerPhotoGalleryComponent.builder()
                .photoGalleryModule(new PhotoGalleryModule(this))
                .ivyAppComponent(((BusinessModel) getApplication()).getComponent())
                .build()
                .inject(this);
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
            Intent resultIntent = new Intent();
            resultIntent.putExtra("edited_data", photoCaptureMap);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
            return true;
        } else if (i == R.id.menu_gallery_share) {
            if (selectedItemsList.size() > 0)
                shareImages();
            else
                showAlertDialog();
            return true;

        } else if (i == R.id.menu_gallery_delete) {
            if (selectedItemsList.size() > 0)
                showDeleteAlertDialog();
            else
                showAlertDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (selectedItemsList.size() <= 0) {
            menu.findItem(R.id.menu_gallery_share).setVisible(false);
            menu.findItem(R.id.menu_gallery_delete).setVisible(false);
        } else {
            menu.findItem(R.id.menu_gallery_share).setVisible(true);
            menu.findItem(R.id.menu_gallery_delete).setVisible(true);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    private void showDeleteAlertDialog() {

        showAlert("", getString(R.string.do_you_want_to_delete_the_image), new CommonDialog.PositiveClickListener() {
            @Override
            public void onPositiveButtonClick() {
                for (String item : selectedItemsList) {
                    if (photoCaptureMap.containsKey(item)) {
                        FileUtils.deleteFiles(getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/"
                                + DataMembers.photoFolderName, photoCaptureMap.get(item).getImageName());

                        photoCaptureMap.remove(item);
                    }
                }
                processData();
            }
        }, new CommonDialog.negativeOnClickListener() {
            @Override
            public void onNegativeButtonClick() {

            }
        });


    }


    /**
     * Alert dialog to select image
     */
    protected void showAlertDialog() {
        showAlert("", "Please Select the Images and Try again!!!");

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
                invalidateOptionsMenu();
            }

            @Override
            public void onPhotoClickRemoved(String selectedItem) {
                if (selectedItemsList.contains(selectedItem))
                    selectedItemsList.remove(selectedItem);

                setSelectionScreenTitle(selectedItemsList.size());
                invalidateOptionsMenu();
            }
        });
        mRecyclerView.setAdapter(photoGalleryAdapter);


    }


    private void shareImages() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND_MULTIPLE);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Pictures");
        intent.setType("image/*");
        ArrayList<Uri> files = new ArrayList<>();
        /* List of the files you want to send */
        for (String path : selectedItemsList) {
            if(photoCaptureMap.get(path)!=null){
                File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/"
                        + DataMembers.photoFolderName + photoCaptureMap.get(path).getImageName());
                if (Build.VERSION.SDK_INT >= 24) {
                    files.add(FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", file));

                } else {
                    files.add(Uri.fromFile(file));
                }
            }
        }
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
        startActivity(Intent.createChooser(intent, "Share Image"));
    }
}
