package com.ivy.sd.png.view;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.ivy.cpg.primarysale.bo.DistributorMasterBO;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;

import java.util.List;

/**
 * Created by rajkumar.s on 9/25/2017.
 */

public class DistributorSelectionActivity extends IvyBaseActivityNoActionBar implements View.OnClickListener {

    private Context mContext;
    private BusinessModel bmodel;
    ListView lvwplist;
    Button bntDownload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        mContext = this;


        setContentView(R.layout.activity_distributor_selection);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        bntDownload = (Button) findViewById(R.id.btn_download);
        bntDownload.setOnClickListener(this);

        TextView toolBarTitle;
        if (toolbar != null)
            setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        toolBarTitle = (TextView) toolbar.findViewById(R.id.tv_toolbar_title);
        toolBarTitle.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

        toolBarTitle.setText(getResources().getString(R.string.distributor));

        getSupportActionBar().setIcon(null);
        // Used to on / off the back arrow icon
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Used to remove the appLogo action bar icon and set title as home
        // (title support click)
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        lvwplist = (ListView) findViewById(R.id.listview);
        lvwplist.setCacheColorHint(0);


        clearAllDistributorStatus();
        DistributorAdapter adapter = new DistributorAdapter(bmodel.distributorMasterHelper.getDistributors());
        lvwplist.setAdapter(adapter);

        findViewById(R.id.btn_download).setOnClickListener(
                this);


    }


    public class DistributorAdapter extends ArrayAdapter<DistributorMasterBO> {
        private List<DistributorMasterBO> items;

        public DistributorAdapter(List<DistributorMasterBO> items) {
            super(mContext, R.layout.row_distributor_selection);
            this.items = items;
        }

        public DistributorMasterBO getItem(int position) {
            return items.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return items.size();
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getLayoutInflater();
                row = inflater.inflate(
                        R.layout.row_distributor_selection, parent,
                        false);
                holder = new ViewHolder();
                holder.distributorName = (TextView) row
                        .findViewById(R.id.tv_distributor_name);
                holder.ChkBox = (CheckBox) row
                        .findViewById(R.id.chkbox);

                holder.ChkBox.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (((CheckBox) v).isChecked()) {
                            updatDistributorStatus(holder.obj.getDId());
                        } else {
                            holder.obj.setChecked(false);
                        }

                    }
                });

                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }
            holder.obj = items.get(position);
            holder.distributorName.setText(holder.obj.getDName());
            holder.ChkBox.setChecked(holder.obj.isChecked());
            return (row);
        }
    }

    class ViewHolder {
        DistributorMasterBO obj;
        TextView distributorName;
        CheckBox ChkBox;
    }

    private void updatDistributorStatus(String distributorId) {
        for (DistributorMasterBO bo : bmodel.distributorMasterHelper.getDistributors()) {
            if (distributorId.equals(bo.getDId())) {
                bo.setChecked(true);

            } else {
                bo.setChecked(false);
            }
        }

        lvwplist.invalidateViews();

    }

    private void clearAllDistributorStatus() {
        for (DistributorMasterBO bo : bmodel.distributorMasterHelper.getDistributors()) {
            bo.setChecked(false);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == bntDownload.getId()) {
            setResult(RESULT_OK);
            finish();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {

            clearAllDistributorStatus();
            setResult(Activity.RESULT_CANCELED);
            finish();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
