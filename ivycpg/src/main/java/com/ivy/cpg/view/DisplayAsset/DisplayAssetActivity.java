package com.ivy.cpg.view.DisplayAsset;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.ivy.cpg.view.asset.bo.AssetTrackingBO;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.view.HomeScreenTwo;

import java.util.ArrayList;

public class DisplayAssetActivity extends IvyBaseActivityNoActionBar {

    private ExpandableListView expandableListView;
    private DisplayAssetHelper displayAssetHelper;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_asset);

        String menuName="";
        if(getIntent().getExtras()!=null)
            menuName=getIntent().getExtras().getString("menuName");

        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {

            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(menuName);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        displayAssetHelper=DisplayAssetHelper.getInstance(this);

        expandableListView=findViewById(R.id.listview_assets);
        expandableListView.setAdapter(new MyAdapter(displayAssetHelper.getDisplayAssetList()));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            startActivity(new Intent(this,
                    HomeScreenTwo.class));
            finish();
            overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
        }

        return super.onOptionsItemSelected(item);
    }

    class MyAdapter extends BaseExpandableListAdapter {

        ArrayList<AssetTrackingBO> assetList;

        public MyAdapter(ArrayList<AssetTrackingBO> assetList){
            this.assetList=assetList;
        }
        @Override
        public Object getChild(int arg0, int arg1) {
            return null;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return 0;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getLayoutInflater();
                row = inflater.inflate(R.layout.row_display_asset_child,
                        parent, false);
                holder = new ViewHolder();
                holder.textView_companyName = row.findViewById(R.id.textview_company_name);
                holder.imageView_minus = row.findViewById(R.id.imiage_minus);
                holder.imageView_plus = row.findViewById(R.id.image_plus);
                holder.editText_quantity = row.findViewById(R.id.image_quantity);


                holder.imageView_minus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        holder.editText_quantity.setText(String.valueOf(doMinus(Integer.parseInt(holder.editText_quantity.getText().toString()))));
                    }
                });
                holder.imageView_plus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        holder.editText_quantity.setText(String.valueOf(doPlus(Integer.parseInt(holder.editText_quantity.getText().toString()))));
                    }
                });


                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            holder.textView_companyName.setText(assetList.get(groupPosition).getCompanyList().get(childPosition).getCompetitorName());

            return row;
        }

        @Override
        public int getChildrenCount(int groupPosition) {


            return assetList.get(groupPosition).getCompanyList().size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return null;
        }

        @Override
        public int getGroupCount() {

            return assetList.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return 0;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {
            final ViewHolder holder;

            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getLayoutInflater();
                row = inflater.inflate(R.layout.row_simple_textview,
                        parent, false);
                holder = new ViewHolder();
                holder.textView_assetName = row.findViewById(R.id.texview_asset_name);


                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            holder.textView_assetName.setText(assetList.get(groupPosition).getAssetName());

            return row;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }

    }

    class ViewHolder {
        TextView textView_assetName,textView_companyName;
        ImageView imageView_plus,imageView_minus;
        EditText editText_quantity;

    }

    public int doMinus(int currentValue){

        if(currentValue>0){
            return currentValue-=1;
        }
        else
        return 0;
    }
    public int doPlus(int currentValue){

            return currentValue+=1;

    }

}
