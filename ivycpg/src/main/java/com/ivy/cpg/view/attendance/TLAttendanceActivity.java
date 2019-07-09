package com.ivy.cpg.view.attendance;

/**
 * Created by rajesh.k on 28-04-2016.
 */

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.core.IvyConstants;
import com.ivy.cpg.view.homescreen.HomeScreenActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.TeamLeadBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.SynchronizationHelper;
import com.ivy.sd.png.view.ReAllocationActivity;

import java.util.ArrayList;


public class TLAttendanceActivity extends IvyBaseActivityNoActionBar {

    private BusinessModel bmodel;
    private ListView mUserLV;
    private ArrayList<TeamLeadBO> mUserNameList;

    TeamLeadReceiver receiver;
    private AlertDialog alertDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tl_attendance);

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        mUserLV = findViewById(R.id.lv_attendance);
        Toolbar toolbar = findViewById(R.id.toolbar);

        if (toolbar != null) {

            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(bmodel.mSelectedActivityName);
            getSupportActionBar().setIcon(null);
//            // Used to on / off the back arrow icon
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//           // Used to remove the app logo actionbar icon and set title as home
//          // (title support click)
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }


        loadData();
        registerReceiver();

    }

    public void loadData() {
        bmodel.teamLeadermasterHelper.downloadUserDetails();
        mUserNameList = bmodel.teamLeadermasterHelper.getUserList();
        mUserLV.setAdapter(new MyAdaper());


    }

    class MyAdaper extends BaseAdapter {

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mUserNameList.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            final ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                LayoutInflater inflater = getLayoutInflater();
                convertView = inflater.inflate(R.layout.list_attendance,
                        parent, false);

                holder.userTV = convertView
                        .findViewById(R.id.tv_user_name);
                holder.previousAttendanceCBOX = convertView
                        .findViewById(R.id.cb_previous_status);
                holder.currentAttendanceCBOX = convertView.findViewById(R.id.cb_current_status);
                holder.currentAttendanceCBOX.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            holder.teamLeadBO.setAttendance(1);
                        } else {
                            holder.teamLeadBO.setAttendance(0);
                        }
                    }
                });


                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.teamLeadBO = mUserNameList.get(position);
            holder.userTV.setText(holder.teamLeadBO
                    .getUserName());
            if (holder.teamLeadBO.getStatus().equals("P")) {
                holder.previousAttendanceCBOX.setChecked(true);
            } else {
                holder.previousAttendanceCBOX.setChecked(false);
            }
            holder.previousAttendanceCBOX.setEnabled(false);


            return convertView;
        }
    }

    class ViewHolder {
        TeamLeadBO teamLeadBO;
        TextView userTV;
        CheckBox previousAttendanceCBOX, currentAttendanceCBOX;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Intent i = new Intent(TLAttendanceActivity.this, HomeScreenActivity.class);
            startActivity(i);
            finish();
            return true;
        } else if (id == R.id.menu_next) {
            nextButtonClick();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void nextButtonClick() {


        new DownloadAbsenteesRetailer().execute();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_only_next, menu);
        return super.onCreateOptionsMenu(menu);
    }


    class DownloadAbsenteesRetailer extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            AlertDialog.Builder builder = new AlertDialog.Builder(TLAttendanceActivity.this);

            customProgressDialog(builder, getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected Void doInBackground(String... params) {
            bmodel.synchronizationHelper.updateAuthenticateToken(false);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            bmodel.synchronizationHelper.downloadAbsenteesRetailer(mUserNameList);


        }
    }

    public class TeamLeadReceiver extends BroadcastReceiver {
        public static final String PROCESS_RESPONSE = "com.ivy.intent.action.AbsenteesRetailerDownload";

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // TODO Auto-generated method stub
            updateReveiver(arg1);
        }

    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter(
                TeamLeadReceiver.PROCESS_RESPONSE);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new TeamLeadReceiver();
        registerReceiver(receiver, filter);
    }


    private void updateReveiver(Intent intent) {
        Bundle bundle = intent.getExtras();
        int method = bundle.getInt(SynchronizationHelper.SYNXC_STATUS, 0);
        String errorCode = bundle.getString(SynchronizationHelper.ERROR_CODE);

        switch (method) {
            case SynchronizationHelper.VOLLEY_TL_ABSENTEES_RETAILER_DOWNLOAD:
                if (!IvyConstants.AUTHENTICATION_SUCCESS_CODE
                        .equals(errorCode)) {

                    alertDialog.dismiss();
                    String errorMsg = bmodel.synchronizationHelper.getErrormessageByErrorCode().get(errorCode);
                    if (errorMsg != null) {
                        Toast.makeText(TLAttendanceActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(TLAttendanceActivity.this, getResources().getString(R.string.data_not_downloaded), Toast.LENGTH_SHORT).show();
                    }

                } else {

                        bmodel.synchronizationHelper.downloadFinishUpdate(SynchronizationHelper.FROM_SCREEN.TL_ALLOCATION, SynchronizationHelper.DOWNLOAD_FINISH_UPDATE,"");


                }
                break;

            case SynchronizationHelper.DOWNLOAD_FINISH_UPDATE:


                alertDialog.dismiss();
                Toast.makeText(TLAttendanceActivity.this, getResources().getString(R.string.data_download_successfully), Toast.LENGTH_SHORT).show();
                bmodel.mSelectedActivityName = "Reallocation";
                Intent i = new Intent(TLAttendanceActivity.this, ReAllocationActivity.class);
                startActivity(i);
                finish();
                break;
        }

    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        unregisterReceiver(receiver);
    }


}
