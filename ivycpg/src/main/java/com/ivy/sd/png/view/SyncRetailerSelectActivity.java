package com.ivy.sd.png.view;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.SyncRetailerBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mayuri.v on 4/18/2017.
 */
public class SyncRetailerSelectActivity extends IvyBaseActivityNoActionBar implements View.OnClickListener {

    private Context mContext;
    private BusinessModel bmodel;
    private List<SyncRetailerBO> isVisitedRetailerList;

    private DialogInterface.OnDismissListener syncRetailerDismissListener;
    private DialogInterface.OnDismissListener syncRetailerBackDismissListener;

    SyncRetailerBO temp;

    ListView lvwplist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //requestWindowFeature(Window.FEATURE_NO_TITLE);

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        mContext = this;
        Bundle bundle = getIntent().getExtras();
        SyncVisitedRetailer catObj=(SyncVisitedRetailer)bundle.getParcelable("list");
        isVisitedRetailerList= catObj.getObjects();
        System.out.println("size=="+isVisitedRetailerList.size());
//        new LoadRetailerIsVisited().execute();

        setContentView(R.layout.dialog_sync_retailer_select);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        TextView toolBarTitle;
        if (toolbar != null)
            setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        toolBarTitle = (TextView) toolbar.findViewById(R.id.tv_toolbar_title);
        toolBarTitle.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

        toolBarTitle.setText("Outlet");

        getSupportActionBar().setIcon(null);
        // Used to on / off the back arrow icon
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Used to remove the appLogo action bar icon and set title as home
        // (title support click)
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        lvwplist = (ListView) findViewById(R.id.dialog_sync_retailer_select_listview);
        lvwplist.setCacheColorHint(0);


        RetailerChooserAdapter adapter = new RetailerChooserAdapter(isVisitedRetailerList);
        lvwplist.setAdapter(adapter);

        findViewById(R.id.dialog_sync_retailer_select_save).setOnClickListener(
                this);
        findViewById(R.id.dialog_sync_retailer_select_back).setOnClickListener(
                this);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {

            setResult(Activity.RESULT_CANCELED);
            finish();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class RetailerChooserAdapter extends ArrayAdapter<SyncRetailerBO> {
        private List<SyncRetailerBO> items;

        public RetailerChooserAdapter(List<SyncRetailerBO> items) {
            super(mContext, R.layout.dialog_sync_retailer_select_listview);
            this.items = items;
        }

        public SyncRetailerBO getItem(int position) {
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
            temp = (SyncRetailerBO) items.get(position);
            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getLayoutInflater();
                row = inflater.inflate(
                        R.layout.dialog_sync_retailer_select_listview, parent,
                        false);
                holder = new ViewHolder();
                holder.retailerName = (TextView) row
                        .findViewById(R.id.dialog_sync_retailer_select_retailername);
                holder.retailerChkBox = (CheckBox) row
                        .findViewById(R.id.dialog_sync_retailer_select_chkbox);

                holder.retailerChkBox.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (((CheckBox) v).isChecked()) {
                            holder.obj.setChecked(true);
                        } else {
                            holder.obj.setChecked(false);
                        }
                    }
                });

                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }
            holder.obj = temp;
            holder.retailerName.setText(temp.getRetailerName());
            holder.retailerChkBox.setChecked(temp.isChecked());
            return (row);
        }
    }

    class ViewHolder {
        SyncRetailerBO obj;
        TextView retailerName;
        CheckBox retailerChkBox;
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (v.getId() == R.id.dialog_sync_retailer_select_save) {
            //syncRetailerDismissListener.onDismiss(this);
            Intent resultIntent = new Intent();
            Bundle b = new Bundle();
            ArrayList<SyncRetailerBO> visitedRetailerList = new ArrayList();
            visitedRetailerList.addAll(isVisitedRetailerList);
            b.putParcelableArrayList("VisitedList", visitedRetailerList);
            resultIntent.putExtras(b);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();


        }
    }



}
