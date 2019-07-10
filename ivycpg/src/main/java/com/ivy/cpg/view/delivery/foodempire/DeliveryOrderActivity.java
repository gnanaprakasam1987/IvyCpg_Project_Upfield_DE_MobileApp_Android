package com.ivy.cpg.view.delivery.foodempire;

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.fragment.app.FragmentManager;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.util.Commons;

/**
 * Created by rajkumar.s on 9/18/2017.
 */

public class DeliveryOrderActivity extends IvyBaseActivityNoActionBar {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_delivery_order);

        Toolbar toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
    }

    public void numberPressed(View vw) {
        FragmentManager fm = getSupportFragmentManager();
        DeliveryOrderFragment asf = (DeliveryOrderFragment) fm
                .findFragmentById(R.id.delivery_order_fragment);
        asf.numberPressed(vw);
    }

    @Override
    public void onBackPressed() {
    }

    protected void onDestroy() {
        super.onDestroy();
        unbindDrawables(findViewById(R.id.root));
        System.gc();
    }

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
                    if (!(view instanceof AdapterView<?>))
                        ((ViewGroup) view).removeAllViews();
                } catch (Exception e) {
                    Commons.printException(e);
                }
            }
        }
    }


}
