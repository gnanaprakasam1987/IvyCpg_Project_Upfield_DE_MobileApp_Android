package com.ivy.sd.png.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.DataMembers;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by dharmapriya.k on 11/28/2016,11:00 AM.
 */
public class ProductDetailsCatalogActivity extends IvyBaseActivityNoActionBar {//implements TabLayout.OnTabSelectedListener {
    private BusinessModel bmodel;
    private Toolbar toolbar;
    private HashMap<Integer, Integer> mSelectedIdByLevelId;
    private ArrayList<String> mProductIdList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail_new);
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        setScreenTitle("Product Details");

        mSelectedIdByLevelId = (HashMap<Integer, Integer>) getIntent().getSerializableExtra("FiveFilter");
        mProductIdList = getIntent().getStringArrayListExtra("ProductIdList");
        ImageView pdt_image_details = (ImageView) findViewById(R.id.pdt_image_details);
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            if (bmodel.configurationMasterHelper.IS_CATALOG_IMG_DOWNLOAD) {

                File prd = new File(getImageFilePath(bmodel.selectedPdt.getProductCode()));
                Glide.with(getApplicationContext())
                        .load(prd)
                        .error(ContextCompat.getDrawable(getApplicationContext(), R.drawable.no_image_available))
                        .dontAnimate()
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .into(pdt_image_details);

            } else {
                pdt_image_details.setImageResource(R.drawable.no_image_available);
            }
        }
        TextView sih_detail = (TextView) findViewById(R.id.sih_detail);
        TextView pdt_name = (TextView) findViewById(R.id.pdt_name);
        sih_detail.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        pdt_name.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

        sih_detail.setText("SIH : " + bmodel.selectedPdt.getSIH());
        pdt_name.setText(bmodel.selectedPdt.getProductName());
        sih_detail.setText("SRP : " + bmodel.selectedPdt.getSrp() + " MRP : " + bmodel.selectedPdt.getMRP() + " SIH : " + bmodel.selectedPdt.getSIH());

        FragmentSchemeDialog fragment = new FragmentSchemeDialog();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.frame, fragment);
        transaction.commit();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        int i = item.getItemId();
        if (i == android.R.id.home) {
            Intent intent = new Intent(ProductDetailsCatalogActivity.this, CatalogOrder.class);
            intent.putExtra("FiveFilter", mSelectedIdByLevelId);
            intent.putStringArrayListExtra("ProductIdList", mProductIdList);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Method used to get image file from sdcard
     *
     * @param fileName
     * @return
     */
    public String getImageFilePath(final String fileName) {

        File file = new File(getExternalFilesDir(
                Environment.DIRECTORY_DOWNLOADS)
                + "/"
                + bmodel.userMasterHelper.getUserMasterBO()
                .getUserid()
                + DataMembers.DIGITAL_CONTENT
                + "/"
                + DataMembers.CATALOG);

        File[] files = file.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                if (pathname.isDirectory()) {
                    return false;
                }

                String name = pathname.getName();
                int lastIndex = name.lastIndexOf('.');
                boolean isFileAvilable = name.startsWith(fileName);

                if (lastIndex < 0 && !isFileAvilable) {
                    return false;
                }
                return (name.substring(lastIndex).equalsIgnoreCase(".png") ||
                        name.substring(lastIndex).equalsIgnoreCase(".jpg"))
                        && isFileAvilable;
            }
        });

        if (files != null && files.length > 0) {
            return files[0].getAbsolutePath();
        }

        return "";
    }

}
