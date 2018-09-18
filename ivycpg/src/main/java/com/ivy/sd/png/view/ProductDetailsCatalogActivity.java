package com.ivy.sd.png.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ivy.cpg.view.order.scheme.SchemeDetailsFragment;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.cpg.view.order.scheme.SchemeDetailsMasterHelper;
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
    private int mTotalScreenWidth = 0;

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
        setScreenTitle(getResources().getString(R.string.Product_details));
        DisplayMetrics dm = new DisplayMetrics();
        getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);
        mTotalScreenWidth = dm.widthPixels;
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
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(pdt_image_details);

            } else {
                pdt_image_details.setImageResource(R.drawable.no_image_available);
            }
        }
        TextView sih_detail = (TextView) findViewById(R.id.sih_detail);
        TextView pdt_name = (TextView) findViewById(R.id.pdt_name);
        sih_detail.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        pdt_name.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        pdt_name.setText(bmodel.selectedPdt.getProductName());
        sih_detail.setText("SIH : " + bmodel.formatValue(bmodel.selectedPdt.getSIH()));
        pdt_name.setText(bmodel.selectedPdt.getProductName());
        StringBuilder sihDetail = new StringBuilder();
        if (bmodel.configurationMasterHelper.SHOW_STK_ORD_SRP) {
            String price;
            if (bmodel.labelsMasterHelper
                    .applyLabels("catalog_srp") != null) {
                price = bmodel.labelsMasterHelper
                        .applyLabels("catalog_srp") + ": ";
            } else {
                price = "Price : ";
            }
            sihDetail.append(price);
            sihDetail.append(bmodel.formatValue(bmodel.selectedPdt.getSrp()));
        }

        if (bmodel.configurationMasterHelper.SHOW_STK_ORD_MRP) {
            sihDetail.append(getResources().getString(R.string.mrp) + ": ");
            sihDetail.append(bmodel.formatValue(bmodel.selectedPdt.getMRP()));
        }

        if (bmodel.configurationMasterHelper.IS_STOCK_IN_HAND) {
            sihDetail.append(getResources().getString(R.string.sih) + ": ");
            sihDetail.append(bmodel.formatValue(bmodel.selectedPdt.getSIH()));
        }
        sih_detail.setText(sihDetail.toString());

        SchemeDetailsMasterHelper schemeHelper=SchemeDetailsMasterHelper.getInstance(getApplicationContext());
        if (schemeHelper.getSchemeList().size() > 0) {

            bmodel.productHelper.setSchemes(schemeHelper.getSchemeList());
            bmodel.productHelper.setPdname(bmodel.selectedPdt.getProductShortName());
            bmodel.productHelper.setProdId(bmodel.selectedPdt.getProductID());
            bmodel.productHelper.setProductObj(bmodel.selectedPdt);
            bmodel.productHelper.setFlag(1);
            bmodel.productHelper.setTotalScreenSize(mTotalScreenWidth);

            SchemeDetailsFragment fragment = new SchemeDetailsFragment();
            Bundle bundle =new Bundle();
            bundle.putString("productId",bmodel.selectedPdt.getProductID());
            fragment.setArguments(bundle);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.frame, fragment);
            transaction.commit();
        }

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
        File file = new File(/*getExternalFilesDir(
                Environment.DIRECTORY_DOWNLOADS)
                + "/"
                + bmodel.userMasterHelper.getUserMasterBO()
                .getUserid()
                + DataMembers.DIGITAL_CONTENT*/
                bmodel.synchronizationHelper.getStorageDir(getResources().getString(R.string.app_name))
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
