package com.ivy.cpg.view.displayscheme;

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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.cpg.view.order.scheme.SchemeDetailsMasterHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.SchemeBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.utils.FontUtils;

import java.util.ArrayList;

/**
 * Created by Rajkumar.S on 8/1/18.
 * Display scheme tracking module
 */

public class DisplaySchemeTrackingActivity extends IvyBaseActivityNoActionBar {


    BusinessModel businessModel;
    RecyclerView recyclerView;
    Toolbar toolbar;
    RecyclerViewAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_display_scheme_tracking);
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

            businessModel = (BusinessModel) getApplicationContext();
            businessModel.setContext(this);

            toolbar = findViewById(R.id.toolbar);
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

            recyclerView = findViewById(R.id.list_scheme);
            recyclerView.setHasFixedSize(false);
            final LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(mLayoutManager);

            SchemeDetailsMasterHelper schemeHelper = SchemeDetailsMasterHelper.getInstance(getApplicationContext());
            mAdapter = new RecyclerViewAdapter(schemeHelper.getDisplaySchemeTrackingList());
            recyclerView.setAdapter(mAdapter);

            Button button_save = findViewById(R.id.btn_next);
            button_save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (SchemeDetailsMasterHelper.getInstance(getApplicationContext()).saveDisplaySchemeTracking(getApplicationContext())) {
                        businessModel.saveModuleCompletion(HomeScreenTwo.MENU_DISPLAY_SCH_TRACK, true);
                        Toast.makeText(DisplaySchemeTrackingActivity.this, getResources().getString(R.string.saved_successfully), Toast.LENGTH_LONG).show();
                        startActivity(new Intent(DisplaySchemeTrackingActivity.this,
                                HomeScreenTwo.class));
                        finish();
                    } else {
                        Toast.makeText(DisplaySchemeTrackingActivity.this, getResources().getString(R.string.saved_Failed), Toast.LENGTH_LONG).show();
                    }
                }
            });


        } catch (Exception ex) {
            Commons.printException(ex);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {

            startActivity(new Intent(DisplaySchemeTrackingActivity.this,
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
                    .inflate(R.layout.row_display_scheme_tracking, parent, false);

            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            final SchemeBO scheme = items.get(position);

            holder.text_scheme_name.setText(scheme.getProductName());
            holder.text_slab_name.setText(scheme.getScheme());

            holder.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (scheme.isSchemeSelected()) {
                        scheme.setSchemeSelected(false);
                        holder.imageView_selected.setVisibility(View.GONE);
                    } else {
                        scheme.setSchemeSelected(true);
                        holder.imageView_selected.setVisibility(View.VISIBLE);
                    }
                }
            });

            if (scheme.isSchemeSelected()) {
                holder.imageView_selected.setVisibility(View.VISIBLE);
            } else {
                holder.imageView_selected.setVisibility(View.GONE);
            }

        }


        @Override
        public int getItemCount() {
            return items.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView text_scheme_name, text_slab_name;
            CardView card;
            ImageView imageView_selected;

            public ViewHolder(View v) {
                super(v);
                text_scheme_name = v.findViewById(R.id.text_scheme_name);
                text_slab_name = v.findViewById(R.id.text_slab_name);
                card = v.findViewById(R.id.card);
                imageView_selected = v.findViewById(R.id.ivAvailable);

                text_scheme_name.setTypeface(FontUtils.getFontRoboto(DisplaySchemeTrackingActivity.this, FontUtils.FontType.MEDIUM));
                text_slab_name.setTypeface(FontUtils.getFontRoboto(DisplaySchemeTrackingActivity.this, FontUtils.FontType.LIGHT));

            }


        }


    }
}
