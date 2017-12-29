package com.ivy.sd.png.view;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_display_scheme);

        businessModel = (BusinessModel) getApplicationContext();
        businessModel.setContext(this);


        recyclerView = (RecyclerView) findViewById(R.id.list_scheme);
        recyclerView.setHasFixedSize(false);
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);

        RecyclerViewAdapter mAdapter = new RecyclerViewAdapter(businessModel.schemeDetailsMasterHelper.getmDisplaySchemeMasterList());
        recyclerView.setAdapter(mAdapter);
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

        }


        @Override
        public int getItemCount() {
            return items.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView text_scheme_name, text_scheme_desc;

            public ViewHolder(View v) {
                super(v);
                text_scheme_name = (TextView) v.findViewById(R.id.text_scheme_name);
                text_scheme_desc = (TextView) v.findViewById(R.id.text_scheme_desc);

                text_scheme_name.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                text_scheme_desc.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

            }


        }
    }
}
