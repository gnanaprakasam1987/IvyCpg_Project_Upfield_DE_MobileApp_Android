package com.ivy.cpg.view.digitalcontent;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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

public class DigitalContentDisplayNew extends IvyBaseActivityNoActionBar {
    private File file;
    private BusinessModel bmodel;
    private String[] folderlist = {"INIT_", "SOD_", "PT_", "SOS_", "SOSKU_", "PL_",
            "CT_", "VPL_", "AT_", "NO_", "SGN_"};
    private ArrayList<ImageBO> imbo, allimagelist;
    private TextView selected_folder;
    private int count = 0;
    private RecyclerView recyclerview, gridrecyclerview;
    private Toolbar toolbar;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_wise_gallery);
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        recyclerview = (RecyclerView) findViewById(R.id.recyclerview);
        gridrecyclerview = (RecyclerView) findViewById(R.id.gridrecyclerview);
        selected_folder = (TextView) findViewById(R.id.selected_folder);
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 3);
        recyclerview.setLayoutManager(layoutManager);
        gridrecyclerview.setLayoutManager(gridLayoutManager);
        try {
            loadImages();
        } catch (Exception e) {
            // TODO Auto-generated catch block
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
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent mintent = new Intent(DigitalContentDisplayNew.this,
                        HomeScreenTwo.class);
                startActivity(mintent);

                finish();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    public void loadImages() {

        String imageInSD = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                + "/" + DataMembers.photoFolderName;
        imbo = new ArrayList<>();
        allimagelist = new ArrayList<>();
        for (int k = 0; k < folderlist.length; k++) {
            file = new File(imageInSD);

            File list[] = file.listFiles();
            ImageBO mimbo = new ImageBO();
            for (int i = 0; i < list.length; i++) {

                String sp = list[i].getName();

                if (sp.startsWith(folderlist[k]
                        + bmodel.getRetailerMasterBO().getRetailerID())) {
                    ImageBO allmimbo = new ImageBO();

                    if (folderlist[k].equals("INIT_")) {
                        mimbo.setImageDirectory("Initiative");
                        allmimbo.setImageDirectory("Initiative");
                        count++;
                    } else if (folderlist[k].equals("PT_")) {
                        mimbo.setImageDirectory("Promotion");
                        allmimbo.setImageDirectory("Promotion");
                        count++;
                    } else if (folderlist[k].equals("SOD_")) {
                        mimbo.setImageDirectory("SOD");
                        allmimbo.setImageDirectory("SOD");
                        count++;
                    } else if (folderlist[k].equals("SOS_")) {
                        count++;
                        mimbo.setImageDirectory("SOS");
                        allmimbo.setImageDirectory("SOS");

                    } else if (folderlist[k].equals("SOSKU_")) {
                        mimbo.setImageDirectory("SOSKU");
                        allmimbo.setImageDirectory("SOSKU");
                        count++;
                    } else if (folderlist[k].equals("PL_")) {
                        mimbo.setImageDirectory("Planogram");
                        allmimbo.setImageDirectory("Planogram");
                        count++;
                    } else if (folderlist[k].equals("CT_")) {
                        mimbo.setImageDirectory("Competitor");
                        allmimbo.setImageDirectory("Competitor");
                        count++;
                    } else if (folderlist[k].equals("VPL_")) {
                        mimbo.setImageDirectory("VanPlanogram");
                        allmimbo.setImageDirectory("VanPlanogram");
                        count++;
                    } else if (folderlist[k].equals("AT_")) {
                        count++;
                        mimbo.setImageDirectory("Asset");
                        allmimbo.setImageDirectory("Asset");
                    } else if (folderlist[k].equals("NO_")) {
                        mimbo.setImageDirectory("RetailerImages");
                        allmimbo.setImageDirectory("RetailerImages");
                        count++;
                    } else if (folderlist[k].equals("SGN_")) {
                        mimbo.setImageDirectory("Invoice");
                        allmimbo.setImageDirectory("Invoice");
                        count++;
                    }
                    mimbo.setCount(count);
                    allmimbo.setImagename(list[i].getName());
                    allmimbo.setImagepath(imageInSD + "/" + list[i].getName());
                    File image = new File(imageInSD + "/" + list[i].getName());
                    InputStream fis = null;
                    try {
                        fis = new FileInputStream(image.getAbsolutePath());
                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        Commons.printException(e);
                    }

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 2;
                    Bitmap bm = BitmapFactory.decodeStream(fis, null, options);
                    mimbo.setFilebitmap(bm);
                    allmimbo.setFilebitmap(bm);
                    allimagelist.add(allmimbo);
                }

            }
            if (count != 0) {
                imbo.add(mimbo);
            }
            count = 0;
        }
        if (imbo.size() == 0) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_data_exists), Toast.LENGTH_SHORT).show();
            Intent mintent = new Intent(DigitalContentDisplayNew.this,
                    HomeScreenTwo.class);
            startActivity(mintent);

            finish();
        } else {
            recyclerview.getLayoutParams().height = 240;
            recyclerview.setAdapter(new CustomGridAdapterDigitalDisplay(getApplicationContext(),
                    imbo, this, selected_folder, gridrecyclerview, allimagelist));
        }
    }

    public void statingactivity(Intent i) {

        startActivity(i);
    }
}