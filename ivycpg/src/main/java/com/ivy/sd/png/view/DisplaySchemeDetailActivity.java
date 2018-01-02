package com.ivy.sd.png.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.SchemeBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

/**
 * Created by Rajkumar on 2/1/18.
 */

public class DisplaySchemeDetailActivity extends IvyBaseActivityNoActionBar {

    BusinessModel businessModel;
    RecyclerView recyclerView;
    Toolbar toolbar;
    int mSelectedSchemeId;

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

                mSelectedSchemeId = extras.getInt("schemeId");
            }

            TextView textView_scheme_desc = (TextView) findViewById(R.id.text_scheme_desc);
            TextView textView_display_period = (TextView) findViewById(R.id.text_display_period);
            TextView textView_booking_period = (TextView) findViewById(R.id.text_booking_period);
            TextView textView_qualifier = (TextView) findViewById(R.id.text_qualifiers);

            for (SchemeBO schemeBO : businessModel.schemeDetailsMasterHelper.getmDisplaySchemeMasterList()) {
                if (schemeBO.getSchemeId().equals(String.valueOf(mSelectedSchemeId))) {
                    textView_scheme_desc.setText(schemeBO.getScheme());
                    textView_display_period.setText(schemeBO.getDisplayPeriodStart() + "-" + schemeBO.getDisplayPeriodEnd());
                }
            }

        } catch (Exception ex) {
            Commons.printException(ex);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            startActivity(new Intent(DisplaySchemeDetailActivity.this,
                    DisplaySchemeActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
