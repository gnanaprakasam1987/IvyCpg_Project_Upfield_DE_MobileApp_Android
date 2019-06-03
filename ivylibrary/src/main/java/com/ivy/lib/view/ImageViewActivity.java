package com.ivy.lib.view;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.ivy.lib.R;
import com.ivy.lib.adapter.ImageViewPagerAdapter;

import java.util.ArrayList;

public class ImageViewActivity extends AppCompatActivity {

    private int selectedPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        final ViewPager imgViewPager = findViewById(R.id.image_view_pager);
        String filePath = getIntent().getStringExtra("FilePath");
        final ArrayList<String> imageList = getIntent().getStringArrayListExtra("imageName");
        selectedPosition = getIntent().getIntExtra("selectedPos", 0);
        getSupportActionBar().setTitle(imageList.get(selectedPosition));

        imgViewPager.setAdapter(new ImageViewPagerAdapter(this, imageList, filePath, selectedPosition));

        imgViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                selectedPosition = position;
                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle(imageList.get(selectedPosition));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        findViewById(R.id.left_img_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    imgViewPager.setCurrentItem(selectedPosition - 1, true);
            }
        });

        findViewById(R.id.right_img_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    imgViewPager.setCurrentItem(selectedPosition + 1, true);
            }
        });


    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
