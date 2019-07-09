package com.ivy.cpg.view.digitalcontent;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ImageBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.view.HomeScreenTwo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Common gallery screen available,so this will be removed soon..
 */
@Deprecated
public class StoreWiseGallery extends IvyBaseActivityNoActionBar {
    private BusinessModel mBModel;
    private String[] nFoldersList = {"INIT_", "SOD_", "PT_", "SOS_", "SOSKU_", "PL_",
            "CT_", "VPL_", "AT_", "NO_", "SGN_"};
    private TextView selected_folder;
    private int count = 0;
    private RecyclerView recyclerview, mGridView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_wise_gallery);
        mBModel = (BusinessModel) getApplicationContext();
        mBModel.setContext(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        recyclerview = (RecyclerView) findViewById(R.id.recyclerview);
        mGridView = (RecyclerView) findViewById(R.id.gridrecyclerview);
        selected_folder = (TextView) findViewById(R.id.selected_folder);
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 3);
        recyclerview.setLayoutManager(layoutManager);
        mGridView.setLayoutManager(gridLayoutManager);
        try {
            loadImages();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(StoreWiseGallery.this,
                        HomeScreenTwo.class);
                startActivity(intent);

                finish();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    public void loadImages() {
        ArrayList<ImageBO> mImageList, mAllImageList;
        File file;
        String imageInSD = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                + "/" + DataMembers.photoFolderName;
        mImageList = new ArrayList<>();
        mAllImageList = new ArrayList<>();
        for (String aNFoldersList : nFoldersList) {
            file = new File(imageInSD);

            File list[] = file.listFiles();
            ImageBO mImageBO = new ImageBO();
            for (File aList : list) {

                String sp = aList.getName();

                if (sp.startsWith(aNFoldersList
                        + mBModel.getRetailerMasterBO().getRetailerID())) {
                    ImageBO mAllImageBO = new ImageBO();

                    switch (aNFoldersList) {
                        case "INIT_":
                            mImageBO.setImageDirectory("Initiative");
                            mAllImageBO.setImageDirectory("Initiative");
                            count++;
                            break;
                        case "PT_":
                            mImageBO.setImageDirectory("Promotion");
                            mAllImageBO.setImageDirectory("Promotion");
                            count++;
                            break;
                        case "SOD_":
                            mImageBO.setImageDirectory("SOD");
                            mAllImageBO.setImageDirectory("SOD");
                            count++;
                            break;
                        case "SOS_":
                            count++;
                            mImageBO.setImageDirectory("SOS");
                            mAllImageBO.setImageDirectory("SOS");

                            break;
                        case "SOSKU_":
                            mImageBO.setImageDirectory("SOSKU");
                            mAllImageBO.setImageDirectory("SOSKU");
                            count++;
                            break;
                        case "PL_":
                            mImageBO.setImageDirectory("Planogram");
                            mAllImageBO.setImageDirectory("Planogram");
                            count++;
                            break;
                        case "CT_":
                            mImageBO.setImageDirectory("Competitor");
                            mAllImageBO.setImageDirectory("Competitor");
                            count++;
                            break;
                        case "VPL_":
                            mImageBO.setImageDirectory("VanPlanogram");
                            mAllImageBO.setImageDirectory("VanPlanogram");
                            count++;
                            break;
                        case "AT_":
                            count++;
                            mImageBO.setImageDirectory("Asset");
                            mAllImageBO.setImageDirectory("Asset");
                            break;
                        case "NO_":
                            mImageBO.setImageDirectory("RetailerImages");
                            mAllImageBO.setImageDirectory("RetailerImages");
                            count++;
                            break;
                        case "SGN_":
                            mImageBO.setImageDirectory("Invoice");
                            mAllImageBO.setImageDirectory("Invoice");
                            count++;
                            break;
                    }
                    mImageBO.setCount(count);
                    mAllImageBO.setImagename(aList.getName());
                    mAllImageBO.setImagepath(imageInSD + "/" + aList.getName());
                    File image = new File(imageInSD + "/" + aList.getName());
                    InputStream fis = null;
                    try {
                        fis = new FileInputStream(image.getAbsolutePath());
                    } catch (FileNotFoundException e) {
                        Commons.printException(e);
                    }

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 2;
                    Bitmap bm = BitmapFactory.decodeStream(fis, null, options);
                    mImageBO.setFilebitmap(bm);
                    mAllImageBO.setFilebitmap(bm);
                    mAllImageList.add(mAllImageBO);
                }

            }
            if (count != 0) {
                mImageList.add(mImageBO);
            }
            count = 0;
        }
        if (mImageList.size() == 0) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_data_exists), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(StoreWiseGallery.this,
                    HomeScreenTwo.class);
            startActivity(intent);

            finish();
        } else {
            recyclerview.getLayoutParams().height = 240;
            recyclerview.setAdapter(new CustomGridAdapterDigitalDisplay(getApplicationContext(),
                    mImageList, this, selected_folder, mGridView, mAllImageList));
        }
    }

    public void startingActivity(Intent i) {
        startActivity(i);
    }
}