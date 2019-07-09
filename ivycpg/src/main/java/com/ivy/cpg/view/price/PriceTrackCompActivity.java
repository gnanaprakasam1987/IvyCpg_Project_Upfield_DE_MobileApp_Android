package com.ivy.cpg.view.price;

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.View;

import androidx.fragment.app.FragmentManager;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;

public class PriceTrackCompActivity extends IvyBaseActivityNoActionBar {

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_price_comp_track);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
    }

    public void numberPressed(View v) {
        FragmentManager fm = getSupportFragmentManager();
        PriceTrackCompFragment fragment = (PriceTrackCompFragment) fm
                .findFragmentById(R.id.price_trackcomp__fragment);
        fragment.numberPressed(v);
    }
}
