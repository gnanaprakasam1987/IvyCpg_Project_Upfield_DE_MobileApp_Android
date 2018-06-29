package com.ivy.sd.png.view;


import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;


/**
 * @See {@link com.ivy.ui.activation.view.ActivationActivity}
 * @since CPG131 replaced by {@link com.ivy.ui.activation.view.ActivationActivity}
 * Will be removed from @version CPG133 Release
 * @deprecated This has been Migrated to MVP pattern
 */
@Deprecated
public class ScreenActivationActivity extends IvyBaseActivityNoActionBar {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_screen_activation);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(null);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

}
