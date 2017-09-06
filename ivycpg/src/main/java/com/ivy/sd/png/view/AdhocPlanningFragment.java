package com.ivy.sd.png.view;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.LocationBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.SynchronizationHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

public class AdhocPlanningFragment extends Fragment {


    View view;
    private BusinessModel bmodel;
    private Spinner userSpin;
    private ListView lv1;
    private ListView lv2;
    private AlertDialog.Builder builder;
    private AlertDialog alertDialog;
    private RetailerSelectionReceiver mRetailerSelectionReceiver;
    private LocationBO mSelectedLocBO;
    private UserMasterBO mSelectedUserBO;
    private HashMap<Integer, ArrayList<RetailerMasterBO>> mRetailerListByLocOrUserId;
    private ArrayList<RetailerMasterBO> mRetailerList;
    private ArrayList<RetailerMasterBO> mFirstRetailerList;
    private ArrayList<RetailerMasterBO> mSecondRetailerList;
    private boolean bool = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        view = inflater.inflate(R.layout.fragment_adhoc_planning, container, false);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        userSpin = (Spinner) view.findViewById(R.id.spn_users);
        Button mAddBtn = (Button) view.findViewById(R.id.btn_add);
        Button mDeleteBtn = (Button) view.findViewById(R.id.btn_delete);
        Button mRefresh1Btn = (Button) view.findViewById(R.id.btn_refresh1);
        Button mRefresh2Btn = (Button) view.findViewById(R.id.btn_refresh2);
        Button mDownloadBtn = (Button) view.findViewById(R.id.btn_ok);
        lv1 = (ListView) view.findViewById(R.id.lv_first);
        lv2 = (ListView) view.findViewById(R.id.lv_second);

        mAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addOrRemoveItem(true);
                updateList();

            }
        });
        mDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addOrRemoveItem(false);
                updateList();
            }
        });
        mRefresh1Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearList(1);
                updateList();
            }
        });
        mRefresh2Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearList(2);
                updateList();
            }
        });
        mDownloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!bool) {
                    bool = true;
                    if (mSecondRetailerList != null && !mSecondRetailerList.isEmpty()) {
                        new DownloadDataByRetailer().execute();

                    } else {
                        bool = false;
                        Toast.makeText(getActivity(), getResources().getString(R.string.please_add_retailer_for_download), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        registerReceiver();
        updateScreen();


        return view;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mRetailerSelectionReceiver);
    }


    private void updateScreen() {
        Vector<LocationBO> locationList;
        ArrayList<UserMasterBO> userList;
        if (!bmodel.configurationMasterHelper.IS_USER_WISE_RETAILER_DOWNLOAD) {
            locationList = bmodel.downloadLocationMaster();
            LocationBO locationBO = new LocationBO();
            locationBO.setLocId(0);
            locationBO.setLocName("-Select");
            locationList.add(0, locationBO);
            ArrayAdapter<LocationBO> locationAdapter = new ArrayAdapter<LocationBO>(getActivity(),
                    R.layout.spinner_bluetext_layout, locationList);
            locationAdapter
                    .setDropDownViewResource(R.layout.spinner_bluetext_list_item);
            userSpin.setAdapter(locationAdapter);
        } else {
            userList = bmodel.userMasterHelper.downloadUserList();
            if (userList == null)
                userList = new ArrayList<>();

            UserMasterBO userMasterBO = new UserMasterBO();
            userMasterBO.setUserid(0);
            userMasterBO.setUserName("-Select");
            userList.add(0, userMasterBO);
            ArrayAdapter<UserMasterBO> userAdapter = new ArrayAdapter<>(
                    getActivity(), R.layout.spinner_bluetext_layout, userList);
            userAdapter
                    .setDropDownViewResource(R.layout.spinner_bluetext_list_item);
            userSpin.setAdapter(userAdapter);
        }


        userSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {


                if (position == 0) {
                    mSelectedUserBO = null;
                    mSelectedLocBO = null;
                    updateList();
                } else {
                    boolean flag = false;
                    if (!bmodel.configurationMasterHelper.IS_USER_WISE_RETAILER_DOWNLOAD)
                        mSelectedLocBO = (LocationBO) userSpin.getSelectedItem();
                    else
                        mSelectedUserBO = (UserMasterBO) userSpin.getSelectedItem();

                    if (mRetailerListByLocOrUserId != null) {
                        ArrayList<RetailerMasterBO> retailerList = mRetailerListByLocOrUserId.get(mSelectedUserBO.getUserid());
                        if (retailerList != null && !retailerList.isEmpty()) {
                            flag = true;
                        }
                    }

                    if (flag) {
                        updateList();
                    } else {
                        new DownloadRetailerByLocation().execute();
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

                // Do nothing
            }
        });
    }

    private void updateList() {

        int locOrUserId = 0;

        if (mSelectedUserBO != null) {
            locOrUserId = mSelectedUserBO.getUserid();
        } else if (mSelectedLocBO != null) {
            locOrUserId = mSelectedLocBO.getLocId();
        }


        if (mRetailerListByLocOrUserId != null) {
            mRetailerList = mRetailerListByLocOrUserId.get(locOrUserId);
            if (mRetailerList != null) {
                mFirstRetailerList = new ArrayList<>();
                mSecondRetailerList = new ArrayList<>();


                for (RetailerMasterBO retailerMasterBO : mRetailerList) {
                    if (!retailerMasterBO.isAddedForDownload()) {
                        mFirstRetailerList.add(retailerMasterBO);
                    } else {
                        mSecondRetailerList.add(retailerMasterBO);
                    }
                }
                if (mFirstRetailerList != null)
                    lv1.setAdapter(new MyAdapter1(mFirstRetailerList));
                if (mSecondRetailerList != null)
                    lv2.setAdapter(new MyAdapter2(mSecondRetailerList));

            } else {

                lv1.setAdapter(new MyAdapter1(new ArrayList<RetailerMasterBO>()));

                lv2.setAdapter(new MyAdapter2(new ArrayList<RetailerMasterBO>()));
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                break;
            default:
                break;
        }
        return true;
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter(
                RetailerSelectionReceiver.RESPONSE);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        mRetailerSelectionReceiver = new RetailerSelectionReceiver();
        getActivity().registerReceiver(mRetailerSelectionReceiver, filter);
    }

    private void updateReceiver(Intent intent) {
        Bundle bundle = intent.getExtras();
        int method = bundle.getInt(SynchronizationHelper.SYNXC_STATUS, 0);
        String errorCode = bundle.getString(SynchronizationHelper.ERROR_CODE);
        label:
        switch (method) {
            case SynchronizationHelper.RETAILER_DOWNLOAD_BY_LOCATION:
                if (errorCode != null && !errorCode
                        .equals(SynchronizationHelper.AUTHENTICATION_SUCCESS_CODE)) {


                    String errorMsg = bmodel.synchronizationHelper.getErrormessageByErrorCode().get(errorCode);
                    if (errorMsg != null) {
                        Toast.makeText(getActivity(), errorMsg, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), getResources().getString(R.string.data_not_downloaded), Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.data_download_successfully), Toast.LENGTH_SHORT).show();
                }
                alertDialog.dismiss();
                updateRetailerByLocOrUser();
                clearList(-1);

                updateList();

                break;
            case SynchronizationHelper.DATA_DOWNLOAD_BY_RETAILER:
                if (errorCode != null && errorCode
                        .equals("1")) {

                    ArrayList<String> urlList = bmodel.synchronizationHelper
                            .getUrlList();
                    if (urlList.size() > 0) {
                        bmodel.synchronizationHelper.downloadMasterAtVolley(SynchronizationHelper.FROM_SCREEN.RETAILER_SELECTION, SynchronizationHelper.DownloadType.RETAILER_WISE_DOWNLOAD);
                    } else {

                        Toast.makeText(getActivity(), getResources().getString(R.string.no_data_download),
                                Toast.LENGTH_SHORT).show();

                        alertDialog.dismiss();
                    }
                } else {
                    String errorMessage = bmodel.synchronizationHelper
                            .getErrormessageByErrorCode().get(errorCode);
                    if (errorMessage != null) {
                        Toast.makeText(getActivity(), errorMessage,
                                Toast.LENGTH_SHORT).show();
                    }

                    alertDialog.dismiss();
                    bool = false;
                }
                break;
            case SynchronizationHelper.VOLLEY_DOWNLOAD_INSERT:
                if (errorCode != null) {
                    switch (errorCode) {
                        case SynchronizationHelper.AUTHENTICATION_SUCCESS_CODE:
                            bmodel.synchronizationHelper.downloadFinishUpdate(SynchronizationHelper.FROM_SCREEN.RETAILER_SELECTION, SynchronizationHelper.RETAILER_DOWNLOAD_FINISH_UPDATE);
                            break;
                        case SynchronizationHelper.UPDATE_TABLE_SUCCESS_CODE:
                            int updateTableCount = bundle.getInt("updateCount");
                            int totalTableCount = bundle.getInt("totalCount");
                            bmodel.updaterProgressMsg(String.format(getResources().getString(R.string.table_downloaded), updateTableCount));
                            if (totalTableCount == (updateTableCount + 1)) {
                                bmodel.updaterProgressMsg(getResources().getString(R.string.updating_tables));
                            }
                            break;
                        default:
                            String errorDownlodCode = bundle
                                    .getString(SynchronizationHelper.ERROR_CODE);
                            String errorDownloadMessage = bmodel.synchronizationHelper
                                    .getErrormessageByErrorCode().get(errorDownlodCode);
                            if (errorDownloadMessage != null) {
                                Toast.makeText(getActivity(), errorDownloadMessage,
                                        Toast.LENGTH_SHORT).show();
                            }
                            alertDialog.dismiss();
                            bmodel.showAlert(getResources().getString(R.string.please_redownload_data), 0);

                            bool = false;

                            break label;
                    }
                }

                break;
            case SynchronizationHelper.RETAILER_DOWNLOAD_FINISH_UPDATE:
                alertDialog.dismiss();
                bool = false;
                Toast.makeText(getActivity(), getResources().getString(R.string.data_download_successfully), Toast.LENGTH_SHORT).show();
                clearList(-1);
                updateList();
                break;

            default:
                break;
        }


    }

    private void addOrRemoveItem(boolean isFirst) {
        if (isFirst) {
            if (mFirstRetailerList != null) {
                for (RetailerMasterBO retailerMasterBO : mFirstRetailerList) {
                    if (retailerMasterBO.isChecked()) {
                        retailerMasterBO.setAddedForDownload(true);
                        retailerMasterBO.setChecked(false);
                    }
                }
            }


        } else {
            if (mSecondRetailerList != null) {
                for (RetailerMasterBO retailerMasterBO : mSecondRetailerList) {
                    if (retailerMasterBO.isChecked()) {
                        retailerMasterBO.setAddedForDownload(false);
                        retailerMasterBO.setChecked(false);
                    }
                }

            }

        }
    }

    private void clearList(int value) {
        if (value == 1) {
            if (mFirstRetailerList != null) {
                for (RetailerMasterBO retailerMasterBO : mFirstRetailerList) {
                    retailerMasterBO.setChecked(false);
                }

            }

        } else if (value == 2) {
            if (mSecondRetailerList != null) {
                for (RetailerMasterBO retailerMasterBO : mSecondRetailerList) {
                    retailerMasterBO.setChecked(false);
                }

            }

        } else if (value == -1) {
            if (mRetailerList != null) {
                for (RetailerMasterBO retailerMasterBO : mRetailerList) {
                    retailerMasterBO.setChecked(false);
                }

            }

        }


    }

    public void updateRetailerByLocOrUser() {
        ArrayList<RetailerMasterBO> retailerList = bmodel.synchronizationHelper.getmRetailerListByLocOrUserWise();
        if (mRetailerListByLocOrUserId == null)
            mRetailerListByLocOrUserId = new HashMap<>();


        if (retailerList != null) {
            if (mSelectedLocBO != null) {
                mRetailerListByLocOrUserId.put(mSelectedLocBO.getLocId(), retailerList);

            } else if (mSelectedUserBO != null) {
                mRetailerListByLocOrUserId.put(mSelectedUserBO.getUserid(), retailerList);
            }
        }


    }

    class MyAdapter1 extends BaseAdapter {
        private ArrayList<RetailerMasterBO> mylist;

        public MyAdapter1(ArrayList<RetailerMasterBO> list) {
            this.mylist = list;

        }

        @Override
        public int getCount() {
            return mylist.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {

                LayoutInflater inflater = getActivity().getLayoutInflater();
                convertView = inflater.inflate(R.layout.list_retailer_selection, parent,
                        false);
                holder = new ViewHolder();
                holder.retailerNameTV = (TextView) convertView.findViewById(R.id.tv_retailer_name);
                holder.retailerSelectCbox = (CheckBox) convertView.findViewById(R.id.cb_retailer_select);
                holder.retailerSelectCbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        holder.retailerMasterBO.setChecked(isChecked);
                    }
                });
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.retailerMasterBO = mylist.get(position);
            holder.retailerNameTV.setText(holder.retailerMasterBO.getRetailerName());
            holder.retailerSelectCbox.setChecked(holder.retailerMasterBO.isChecked());
            return convertView;
        }
    }

    class MyAdapter2 extends BaseAdapter {
        private ArrayList<RetailerMasterBO> mylist;

        public MyAdapter2(ArrayList<RetailerMasterBO> list) {
            this.mylist = list;

        }

        @Override
        public int getCount() {
            return mylist.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {

                LayoutInflater inflater = getActivity().getLayoutInflater();
                convertView = inflater.inflate(R.layout.list_retailer_selection, parent,
                        false);
                holder = new ViewHolder();
                holder.retailerNameTV = (TextView) convertView.findViewById(R.id.tv_retailer_name);
                holder.retailerSelectCbox = (CheckBox) convertView.findViewById(R.id.cb_retailer_select);
                holder.retailerSelectCbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        holder.retailerMasterBO.setChecked(isChecked);
                    }
                });
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.retailerMasterBO = mylist.get(position);
            holder.retailerNameTV.setText(holder.retailerMasterBO.getRetailerName());
            holder.retailerSelectCbox.setChecked(holder.retailerMasterBO.isChecked());
            return convertView;
        }
    }

    class ViewHolder {
        CheckBox retailerSelectCbox;
        TextView retailerNameTV;
        RetailerMasterBO retailerMasterBO;
    }

    class DownloadRetailerByLocation extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            builder = new AlertDialog.Builder(getActivity());

            bmodel.customProgressDialog(alertDialog, builder, getActivity(), getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();

        }

        @Override
        protected Void doInBackground(String... params) {
            bmodel.synchronizationHelper.updateAuthenticateToken();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!bmodel.configurationMasterHelper.IS_USER_WISE_RETAILER_DOWNLOAD && mSelectedLocBO != null) {
                bmodel.synchronizationHelper.downloadRetailerByLocFromServer(mSelectedLocBO.getLocId(), false);
            } else {
                if (mSelectedUserBO != null)
                    bmodel.synchronizationHelper.downloadRetailerByLocFromServer(mSelectedUserBO.getUserid(), false);
            }

        }
    }

    class DownloadDataByRetailer extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            builder = new AlertDialog.Builder(getActivity());

            bmodel.customProgressDialog(alertDialog, builder, getActivity(), getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected Void doInBackground(String... params) {
            bmodel.synchronizationHelper.updateAuthenticateToken();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            ArrayList<RetailerMasterBO> retailerList = new ArrayList<>();
            retailerList.addAll(mSecondRetailerList);

            ArrayList<String> temp = new ArrayList<>();
            for (RetailerMasterBO bo : mSecondRetailerList) {
                temp.add(bo.getRetailerID());
            }

            for (RetailerMasterBO bo : bmodel.getRetailerMaster()) {
                if (bo.getIsPlanned().equals("Y") && !temp.contains(bo.getRetailerID())) {
                    retailerList.add(bo);
                }
            }
            bmodel.synchronizationHelper.downloadMasterListBySelectedRetailer(retailerList, SynchronizationHelper.FROM_SCREEN.RETAILER_SELECTION);
        }
    }

    public class RetailerSelectionReceiver extends BroadcastReceiver {
        public static final String RESPONSE = "com.ivy.intent.action.RetailerSelectionReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            updateReceiver(intent);
        }
    }

}
