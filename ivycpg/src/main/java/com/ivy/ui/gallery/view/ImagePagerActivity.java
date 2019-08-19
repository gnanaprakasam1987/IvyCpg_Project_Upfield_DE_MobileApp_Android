package com.ivy.ui.gallery.view;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.base.view.BaseActivity;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.cpg.view.basedi.BaseModule;
import com.ivy.cpg.view.basedi.DaggerBaseComponent;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.ui.gallery.adapter.ImagePagerAdapter;
import com.ivy.ui.gallery.model.GalleryBo;

import java.util.ArrayList;

import javax.inject.Inject;

public class ImagePagerActivity extends BaseActivity {
    private int selectedPosition = 0;

    @Inject
    BaseIvyPresenter<BaseIvyView> viewBasePresenter;

    @Override
    public int getLayoutId() {
        return R.layout.activity_image_pager;
    }

    @Override
    protected void initVariables() {

    }

    @Override
    public void initializeDi() {
        DaggerBaseComponent.builder()
                .baseModule(new BaseModule(this))
                .ivyAppComponent(((BusinessModel) getApplication()).getComponent())
                .build()
                .inject(this);

        setBasePresenter((BasePresenter) viewBasePresenter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void getMessageFromAliens() {

    }

    @Override
    protected void setUpViews() {
        ArrayList<GalleryBo> galleryBoArrayList = getIntent().getExtras().getParcelableArrayList("galleryList");
        selectedPosition = getIntent().getIntExtra("curPos", 0);


        Toolbar toolbar = findViewById(com.ivy.lib.R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        getSupportActionBar().setTitle(galleryBoArrayList.get(selectedPosition).getImageName());

        ViewPager imgViewPager = findViewById(R.id.view_pager);
        imgViewPager.setPageTransformer(true, new DefaultPageTransformer());

        imgViewPager.setAdapter(new ImagePagerAdapter(getSupportFragmentManager(), galleryBoArrayList));


        imgViewPager.setCurrentItem(selectedPosition);
        imgViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                selectedPosition = position;
                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle(galleryBoArrayList.get(position).getImageName());

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }


}
