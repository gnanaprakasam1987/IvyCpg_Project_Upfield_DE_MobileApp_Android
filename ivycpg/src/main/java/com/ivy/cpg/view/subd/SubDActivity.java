package com.ivy.cpg.view.subd;

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;

public class SubDActivity extends IvyBaseActivityNoActionBar {
    BusinessModel bmodel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_d);

        bmodel = (BusinessModel) this.getApplicationContext();
        bmodel.setContext(this);

    }

}
