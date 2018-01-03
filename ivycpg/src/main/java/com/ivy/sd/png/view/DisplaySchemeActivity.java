package com.ivy.sd.png.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.SchemeBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;

import java.util.ArrayList;

/**
 * Created by Rajkumar.S on 29/12/17.
 *
 */

public class DisplaySchemeActivity extends IvyBaseActivityNoActionBar {

    BusinessModel businessModel;
    RecyclerView recyclerView;
    Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_display_scheme);
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
            String title = extras.getString("menuName") == null ? "" : extras.getString("menuName");
            setScreenTitle(title);
        }


        recyclerView = (RecyclerView) findViewById(R.id.list_scheme);
        recyclerView.setHasFixedSize(false);
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);

        RecyclerViewAdapter mAdapter = new RecyclerViewAdapter(businessModel.schemeDetailsMasterHelper.getmDisplaySchemeMasterList());
        recyclerView.setAdapter(mAdapter);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            startActivity(new Intent(DisplaySchemeActivity.this,
                    HomeScreenTwo.class));
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

        private ArrayList<SchemeBO> items;

        public RecyclerViewAdapter(ArrayList<SchemeBO> items) {
            this.items = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_display_scheme, parent, false);

            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            final SchemeBO scheme = items.get(position);

            holder.text_scheme_name.setText(scheme.getProductName());
            holder.text_scheme_desc.setText(scheme.getScheme());

            holder.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(DisplaySchemeActivity.this, DisplaySchemeDetailActivity.class);
                    intent.putExtra("menuName", getIntent().getExtras().getString("menuName"));
                    intent.putExtra("schemeName", scheme.getProductName());
                    intent.putExtra("schemeId", scheme.getSchemeId());
                    startActivity(intent);
                    //finish();
                }
            });

        }


        @Override
        public int getItemCount() {
            return items.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView text_scheme_name, text_scheme_desc;
            CardView card;

            public ViewHolder(View v) {
                super(v);
                text_scheme_name = (TextView) v.findViewById(R.id.text_scheme_name);
                text_scheme_desc = (TextView) v.findViewById(R.id.text_scheme_desc);
                card = (CardView) v.findViewById(R.id.card);

                text_scheme_name.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                text_scheme_desc.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

            }


        }
    }
}
