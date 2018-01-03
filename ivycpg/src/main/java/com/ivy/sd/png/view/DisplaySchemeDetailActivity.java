package com.ivy.sd.png.view;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

import static android.content.res.Configuration.SCREENLAYOUT_SIZE_LARGE;
import static com.ivy.sd.png.asean.view.R.id.tab_layout;

/**
 * Created by Rajkumar on 2/1/18.
 */

public class DisplaySchemeDetailActivity extends IvyBaseActivityNoActionBar {

    BusinessModel businessModel;
    Toolbar toolbar;
    String mSelectedSchemeId;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    boolean is7InchTablet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_display_scheme_detail);
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

            businessModel = (BusinessModel) getApplicationContext();
            businessModel.setContext(this);

            toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayShowTitleEnabled(false);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }

            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                String title = extras.getString("schemeName") == null ? "" : extras.getString("schemeName");
                setScreenTitle(title);

                mSelectedSchemeId = extras.getString("schemeId");
            }

            tabLayout = (TabLayout) findViewById(tab_layout);
            viewPager = (ViewPager) findViewById(R.id.pager);

            tabLayout.addTab(tabLayout.newTab()
                    .setText("Info"));
            tabLayout.addTab(tabLayout.newTab()
                    .setText("Slab"));

            tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
            is7InchTablet = this.getResources().getConfiguration()
                    .isLayoutSizeAtLeast(SCREENLAYOUT_SIZE_LARGE);
            if (!is7InchTablet && tabLayout.getTabCount() > 3) {
                tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
            }

            final ViewPagerAdapter adapter = new ViewPagerAdapter
                    (this.getSupportFragmentManager(), tabLayout.getTabCount(), null);
            viewPager.setAdapter(adapter);
            viewPager.setOffscreenPageLimit(tabLayout.getTabCount());
            viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    viewPager.setCurrentItem(tab.getPosition());
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                }
            });
           /* TextView header_label_products=(TextView)findViewById(R.id.header_label_products);
            TextView header_label_slab=(TextView)findViewById(R.id.header_label_slab);
            header_label_products.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            header_label_slab.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

            findViewById(R.id.card).setLayerType(View.LAYER_TYPE_SOFTWARE, null);


            LinearLayout layout_slab=(LinearLayout)findViewById(R.id.layout_slab);
           // layout_slab.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            View slabView;
            LayoutInflater inflater = LayoutInflater.from(this);
            ArrayList<SchemeBO> mSlabList=businessModel.schemeDetailsMasterHelper.downloadDisplaySchemeSlabs(getApplicationContext(),mSelectedSchemeId);
            for(SchemeBO bo:mSlabList){
                slabView = inflater.inflate(R.layout.row_display_scheme_slabs, null);
                slabView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

                TextView text_slabName=(TextView)slabView.findViewById(R.id.text_slab_name);
                TextView text_getType=(TextView)slabView.findViewById(R.id.text_type);
                TextView text_Value=(TextView)slabView.findViewById(R.id.text_value);

                text_slabName.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                text_getType.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                text_Value.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

                text_slabName.setText(bo.getScheme());
                text_getType.setText(bo.getGetType());
                text_Value.setText(bo.getDisplaySchemeValue());

                layout_slab.addView(slabView);
            }

            ArrayList<Integer> mProductIdList=businessModel.schemeDetailsMasterHelper.downloadDisplaySchemeProducts(getApplicationContext(),mSelectedSchemeId);
           *//* LinearLayout layout_products=(LinearLayout)findViewById(R.id.layout_products);
            for(Integer productId:mProductIdList){
                ProductMasterBO productMasterBO =businessModel.productHelper.getProductMasterBOById(String.valueOf(productId));
                if(productMasterBO!=null){
                    TextView textView = new TextView(this);
                    textView.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

                    textView.setText(productMasterBO.getProductName());
                    layout_products.addView(textView);
                }
            }*/

        } catch (Exception ex) {
            Commons.printException(ex);
        }
    }

    /**
     * ViewPagerAdapter class used to call fragment dynamically.
     */
    public class ViewPagerAdapter extends FragmentPagerAdapter {

        private final int mNumOfTabs;
        private final Bundle bundleAdapter;

        ViewPagerAdapter(FragmentManager fm, int NumOfTabs, Bundle bundleAdapter) {
            super(fm);
            this.mNumOfTabs = NumOfTabs;
            this.bundleAdapter = bundleAdapter;
        }

        @Override
        public Fragment getItem(int position) {

            String tabName = tabLayout.getTabAt(position).getText().toString();
            if (tabName.equalsIgnoreCase("Info")) {
                DisplaySchemeInfoFragment fragment = new DisplaySchemeInfoFragment();
                return fragment;
            } else if (tabName.equals("Slab")) {
                return new DisplaySchemeSlabFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            return mNumOfTabs;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
           /* Intent intent=new Intent(DisplaySchemeDetailActivity.this,DisplaySchemeActivity.class);
            intent.putExtra("menuName",getIntent().getExtras().getString("menuName"));*/
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
