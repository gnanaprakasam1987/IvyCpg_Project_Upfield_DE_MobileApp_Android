package com.ivy.sd.png.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.widget.Toolbar;
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
import com.ivy.sd.png.util.Commons;

import java.util.List;

/**
 * Created by rajkumar.s on 9/25/2017.
 */

public class DistributorSelectionActivity extends IvyBaseActivityNoActionBar implements View.OnClickListener {

    private Context mContext;
    private BusinessModel bmodel;
    ListView lvwplist;
    Button bntDownload;
    SharedPreferences.Editor editor;
    SharedPreferences sharedPrefs;
    boolean isFromLogin=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {

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
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            // Used to remove the appLogo action bar icon and set title as home
            // (title support click)
            getSupportActionBar().setDisplayShowHomeEnabled(true);

            if(getIntent().getExtras()!=null){
                isFromLogin=getIntent().getExtras().getBoolean("isFromLogin");
            }

            lvwplist = (ListView) findViewById(R.id.listview);
            lvwplist.setCacheColorHint(0);


            clearAllDistributorStatus();
            DistributorAdapter adapter = new DistributorAdapter(bmodel.distributorMasterHelper.getDistributors());
            lvwplist.setAdapter(adapter);

            findViewById(R.id.btn_download).setOnClickListener(
                    this);

            sharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(this);
            editor = sharedPrefs.edit();




        }
        catch (Exception ex){
            Commons.printException(ex);
        }


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
                        try {
                            if (((CheckBox) v).isChecked()) {

                                if (!isFromLogin&&!holder.obj.getDId().equals(sharedPrefs.getString("Did", "0"))
                                        &&!bmodel.synchronizationHelper.isDayClosed()){

                                    holder.ChkBox.setChecked(false);
                                    bmodel.showAlert(
                                            getResources().getString(
                                                    R.string.please_close_the_day_for_current_Distributor), 0);
                                }
                                else{
                                    //Same distributor or if current distributor day closed
                                    updatDistributorStatus(holder.obj.getDId());
                                }

                            } else {
                                holder.obj.setChecked(false);
                            }
                        }
                        catch (Exception ex){
                            Commons.printException(ex);
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

    private void updatDistributorStatus(String distributorId){
        try{
            for(DistributorMasterBO bo:bmodel.distributorMasterHelper.getDistributors()){
                if(distributorId.equals(bo.getDId())){
                    bo.setChecked(true);

                    editor.putString("Did", distributorId);
                    editor.commit();

                }
                else{
                    bo.setChecked(false);
                }
            }

            lvwplist.invalidateViews();
        }
        catch (Exception ex){
            Commons.printException(ex);
        }
    }

    private void clearAllDistributorStatus(){
        try{
            for(DistributorMasterBO bo:bmodel.distributorMasterHelper.getDistributors()){
                bo.setChecked(false);
            }
        }
        catch (Exception ex){
            Commons.printException(ex);
        }
    }
    @Override
    public void onClick(View view) {
        try {
            if (view.getId() == bntDownload.getId()) {

                if (isDistributorSelected()) {

                    /*String did = sharedPrefs.getString("Did", "");
                    if (!did.equals("")) {
                        bmodel.userMasterHelper.updateDistributorId(did);
                    }*/
                    setResult(RESULT_OK);
                    finish();
                } else {
                    bmodel.showAlert(
                            getResources().getString(
                                    R.string.please_select_a_Distributor_to_download), 0);
                }
            }
        }
        catch (Exception ex){
            Commons.printException(ex);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
             /*   clearAllDistributorStatus();
                setResult(Activity.RESULT_CANCELED);
                finish();*/


            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private boolean isDistributorSelected(){
        try{
            for(DistributorMasterBO bo:bmodel.distributorMasterHelper.getDistributors()){
                if(bo.isChecked()){
                    return true;
                }
            }
        }
        catch (Exception ex){
            Commons.printException(ex);
        }
        return false;
    }
}
