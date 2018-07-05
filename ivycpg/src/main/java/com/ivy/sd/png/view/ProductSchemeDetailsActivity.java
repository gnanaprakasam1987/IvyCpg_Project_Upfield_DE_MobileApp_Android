package com.ivy.sd.png.view;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.ivy.cpg.view.order.scheme.SchemeDetailsFragment;
import com.ivy.sd.png.asean.view.BuildConfig;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by nagaganesh.n on 4/27/2017.
 */

public class ProductSchemeDetailsActivity extends IvyBaseActivityNoActionBar {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ArrayList<Fragment> mFragmentList;
    private Fragment mSelectFragment, mSelectedFragment;
    private TabLayout.Tab schemeDetailsTab;
    private TabLayout.Tab productDetailsTab;
    private ViewPager viewPager;
    private Bundle instate;
    private BusinessModel bmodel;
    private String productId="0";
    private boolean isFromUpSelling;
    private ImageView pdt_image;
    private File appImageFolderPath;
    AppBarLayout appbar;
    NestedScrollView nestedScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_product_scheme_details);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        isFromUpSelling=getIntent().getBooleanExtra("isFromUpSelling",false);

        if(getIntent()!=null&&getIntent().getStringExtra("productId")!=null){
            productId=String.valueOf(getIntent().getStringExtra("productId"));
        }

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        addFragments();

        ViewGroup vg = (ViewGroup) tabLayout.getChildAt(0);
        int tabsCount = vg.getChildCount();
        for (int j = 0; j < tabsCount; j++) {
            ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
            int tabChildsCount = vgTab.getChildCount();
            for (int i = 0; i < tabChildsCount; i++) {
                View tabViewChild = vgTab.getChildAt(i);
                if (tabViewChild instanceof TextView) {
                    ((TextView) tabViewChild).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                }
            }
        }

        viewPager = (ViewPager) findViewById(R.id.pager);
        TabsPagerAdapter mAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(mAdapter);
        tabLayout.setupWithViewPager(viewPager);
        viewPager.setOffscreenPageLimit(1);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {


                mSelectedFragment = mFragmentList.get(position);

                mSelectedFragment.onResume();

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mAdapter.notifyDataSetChanged();

        if (tabLayout != null)
            tabLayout.removeAllTabs();

        schemeDetailsTab = tabLayout.newTab();
        try {
            if (bmodel.labelsMasterHelper.applyLabels("scheme_details_tab") != null)
                schemeDetailsTab.setText(bmodel.labelsMasterHelper.applyLabels("scheme_details_tab"));
            else
                schemeDetailsTab.setText("Scheme");
        } catch (Exception e) {
            Commons.printException(e);
            schemeDetailsTab.setText("Scheme");
        }
        tabLayout.addTab(schemeDetailsTab);

        if (bmodel.configurationMasterHelper.IS_PRODUCT_SCHEME_DIALOG) {
            productDetailsTab = tabLayout.newTab();
            try {
                if (bmodel.labelsMasterHelper.applyLabels("product_details_tab") != null)
                    productDetailsTab.setText(bmodel.labelsMasterHelper.applyLabels("product_details_tab"));
                else
                    productDetailsTab.setText(getResources().getString(R.string.Product_details));
            } catch (Exception e) {
                Commons.printException(e);
                productDetailsTab.setText(getResources().getString(R.string.Product_details));
            }
            tabLayout.addTab(productDetailsTab);
        }

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        if(isFromUpSelling)
            tabLayout.setVisibility(View.GONE);

        try {
            pdt_image = findViewById(R.id.pdt_image);
            appbar = findViewById(R.id.appbar);
            nestedScrollView = findViewById(R.id.scrollView);
            appImageFolderPath = bmodel.synchronizationHelper.getStorageDir(getResources().getString(R.string.app_name));
            if (pdt_image != null) {
                Uri path;
                if (Build.VERSION.SDK_INT >= 24) {
                    path = FileProvider.getUriForFile(ProductSchemeDetailsActivity.this, BuildConfig.APPLICATION_ID + ".provider", new File(
                            appImageFolderPath
                                    + "/"
                                    + DataMembers.CATALOG + "/" + bmodel.productHelper.getProductObj().getProductCode() + ".jpg"));
                } else {
                    path = Uri.fromFile(new File(
                            appImageFolderPath
                                    + "/"
                                    + DataMembers.CATALOG + "/" + bmodel.productHelper.getProductObj().getProductCode() + ".jpg"));
                }

                //Set Image in Imageview using Glide, on exception disable scrolling of ImageView
                Glide.with(getApplicationContext())
                        .load(path)
                        .error(ContextCompat.getDrawable(getApplicationContext(), R.drawable.no_image_available))
                        .dontAnimate()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .listener(new RequestListener<Uri, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
                                appbar.setExpanded(false);
                                ViewCompat.setNestedScrollingEnabled(nestedScrollView, false);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                return false;
                            }
                        })
                        .into(pdt_image);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    protected void passData(Bundle instate) {
        this.instate = instate;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindDrawables(findViewById(R.id.root));
    }

    /**
     * this would clear all the resources used of the layout.
     *
     * @param view parentView
     */
    private void unbindDrawables(View view) {
        if (view != null) {
            if (view.getBackground() != null) {
                view.getBackground().setCallback(null);
            }
            if (view instanceof ViewGroup) {
                for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                    unbindDrawables(((ViewGroup) view).getChildAt(i));
                }
                try {
                    ((ViewGroup) view).removeAllViews();
                } catch (Exception e) {
                    Commons.printException(e);
                }
            }
        }
    }


    //Method used to add fragments for Tablayout
    private void addFragments() {

        mFragmentList = new ArrayList<>();
        mSelectFragment = new SchemeDetailsFragment();
        Bundle bundle =new Bundle();
        bundle.putString("productId",productId);
        if(getIntent()!=null&&getIntent().getStringExtra("slabId")!=null)
          bundle.putString("slabId",getIntent().getStringExtra("slabId"));

        mSelectFragment.setArguments(bundle);
        mFragmentList.add(mSelectFragment);



        if (!isFromUpSelling&&bmodel.configurationMasterHelper.IS_PRODUCT_SCHEME_DIALOG) {
            mSelectFragment = new ProductDetailsFragment();
            mFragmentList.add(mSelectFragment);
        }
    }

    /**
     * This class used to contained all the fragments.
     */
    private class TabsPagerAdapter extends FragmentPagerAdapter {

        FragmentManager fragmentManager;

        public TabsPagerAdapter(FragmentManager fm) {
            super(fm);
            this.fragmentManager = fm;
        }

        @Override
        public Fragment getItem(int index) {
            return mFragmentList.get(index);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_product_scheme_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);

            return true;
        }
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
