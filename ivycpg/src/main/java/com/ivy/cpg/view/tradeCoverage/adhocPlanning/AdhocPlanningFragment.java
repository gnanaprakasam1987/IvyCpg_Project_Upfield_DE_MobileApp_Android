package com.ivy.cpg.view.tradeCoverage.adhocPlanning;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.core.IvyConstants;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.BeatMasterBO;
import com.ivy.sd.png.bo.LocationBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.SynchronizationHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

public class AdhocPlanningFragment extends IvyBaseFragment {


    View view;
    private BusinessModel bmodel;
    private Spinner userSpin;
    private Spinner beatSpin;
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
    private int mSelectecBeatId = 0;
    private boolean bool = false;
    private EditText mRetailerEdt;
    private ImageView mSearchIv;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        view = inflater.inflate(R.layout.fragment_adhoc_planning, container, false);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        userSpin = view.findViewById(R.id.spn_users);
        beatSpin = view.findViewById(R.id.spn_beat);
        Button mAddBtn = view.findViewById(R.id.btn_add);
        Button mDeleteBtn = view.findViewById(R.id.btn_delete);
        Button mRefresh1Btn = view.findViewById(R.id.btn_refresh1);
        Button mRefresh2Btn = view.findViewById(R.id.btn_refresh2);
        Button mDownloadBtn = view.findViewById(R.id.btn_ok);
        mDownloadBtn.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        lv1 = view.findViewById(R.id.lv_first);
        lv2 = view.findViewById(R.id.lv_second);

        //Retailer Search Widgets
        mRetailerEdt = view.findViewById(R.id.input_retailer);
        mSearchIv = view.findViewById(R.id.ivSearch);

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

        mSearchIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRetailerEdt.getText().toString().trim().isEmpty()) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.newretailer_empty), Toast.LENGTH_SHORT).show();
                } else {
                    new DownloadRetailerByLocation().execute();
                }

            }
        });

        if (bmodel.configurationMasterHelper.IS_RET_NAME_RETAILER_DOWNLOAD) {
            view.findViewById(R.id.llSpinner).setVisibility(View.GONE);
            view.findViewById(R.id.llRetailers).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.llSpinner).setVisibility(View.VISIBLE);
            view.findViewById(R.id.llRetailers).setVisibility(View.GONE);
        }
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
        if (!bmodel.configurationMasterHelper.IS_RET_NAME_RETAILER_DOWNLOAD) {
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
                userList = bmodel.userMasterHelper.downloadAdHocUserList();
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

            if (bmodel.configurationMasterHelper.IS_BEAT_WISE_RETAILER_DOWNLOAD) {
                ArrayList<BeatMasterBO> beatList = new ArrayList<>();
                BeatMasterBO beatMasterBO = new BeatMasterBO();
                beatMasterBO.setBeatId(0);
                beatMasterBO.setBeatDescription(getResources().getString(R.string.select_beat));
                beatList.add(0, beatMasterBO);
                ArrayAdapter<BeatMasterBO> beatAdapter = new ArrayAdapter<BeatMasterBO>(getActivity(),
                        R.layout.spinner_bluetext_layout, beatList);
                beatAdapter
                        .setDropDownViewResource(R.layout.spinner_bluetext_list_item);
                beatSpin.setAdapter(beatAdapter);
            } else
                beatSpin.setVisibility(View.GONE);
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
                    if (!bmodel.configurationMasterHelper.IS_USER_WISE_RETAILER_DOWNLOAD) {

                        mSelectedLocBO = (LocationBO) userSpin.getSelectedItem();
                        if (mRetailerListByLocOrUserId != null) {
                            ArrayList<RetailerMasterBO> retailerList = mRetailerListByLocOrUserId.get(mSelectedLocBO.getLocId());
                            if (retailerList != null && !retailerList.isEmpty()) {
                                flag = true;
                            }
                        }
                    } else {
                        mSelectedUserBO = (UserMasterBO) userSpin.getSelectedItem();
                        if (mSelectedUserBO != null && bmodel.configurationMasterHelper.IS_BEAT_WISE_RETAILER_DOWNLOAD) {
                            ArrayList<BeatMasterBO> beatList = new ArrayList<>();

                            beatList = bmodel.beatMasterHealper.downloadBeats(mSelectedUserBO.getUserid());
                            if (beatList == null)
                                beatList = new ArrayList<>();

                            BeatMasterBO beatMasterBO = new BeatMasterBO();
                            beatMasterBO.setBeatId(0);
                            beatMasterBO.setBeatDescription(getResources().getString(R.string.all));
                            beatList.add(0, beatMasterBO);
                            ArrayAdapter<BeatMasterBO> beatAdapter = new ArrayAdapter<BeatMasterBO>(getActivity(),
                                    android.R.layout.simple_spinner_item, beatList);
                            beatSpin.setAdapter(beatAdapter);
                        }
                        if (mRetailerListByLocOrUserId != null) {
                            ArrayList<RetailerMasterBO> retailerList = mRetailerListByLocOrUserId.get(mSelectedUserBO.getUserid());
                            if (retailerList != null && !retailerList.isEmpty()) {
                                flag = true;
                            }
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

        beatSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                BeatMasterBO beatMasterBO = (BeatMasterBO) beatSpin.getSelectedItem();
                mSelectecBeatId = beatMasterBO.getBeatId();
                updateList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    private void updateList() {

        int locOrUserId = 0;

        if (!bmodel.configurationMasterHelper.IS_RET_NAME_RETAILER_DOWNLOAD) {
            if (!bmodel.configurationMasterHelper.IS_USER_WISE_RETAILER_DOWNLOAD) {
                if (mSelectedLocBO != null) {
                    locOrUserId = mSelectedLocBO.getLocId();
                }
            } else {
                if (mSelectedUserBO != null) {
                    locOrUserId = mSelectedUserBO.getUserid();
                }
            }
        }


        if (mRetailerListByLocOrUserId != null) {
            mRetailerList = mRetailerListByLocOrUserId.get(locOrUserId);
            if (mRetailerList != null) {
                mFirstRetailerList = new ArrayList<>();
                mSecondRetailerList = new ArrayList<>();


                for (RetailerMasterBO retailerMasterBO : mRetailerList) {
                    if (mSelectecBeatId != 0 && bmodel.configurationMasterHelper.IS_BEAT_WISE_RETAILER_DOWNLOAD) {
                        if (retailerMasterBO.getBeatID() == mSelectecBeatId) {
                            if (!retailerMasterBO.isAddedForDownload()) {
                                mFirstRetailerList.add(retailerMasterBO);
                            } else {
                                mSecondRetailerList.add(retailerMasterBO);
                            }
                        }
                    } else {
                        if (!retailerMasterBO.isAddedForDownload()) {
                            mFirstRetailerList.add(retailerMasterBO);
                        } else {
                            mSecondRetailerList.add(retailerMasterBO);
                        }
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
                        .equals(IvyConstants.AUTHENTICATION_SUCCESS_CODE)) {


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
                        case IvyConstants.AUTHENTICATION_SUCCESS_CODE:
                            bmodel.synchronizationHelper.downloadFinishUpdate(SynchronizationHelper.FROM_SCREEN.RETAILER_SELECTION, SynchronizationHelper.RETAILER_DOWNLOAD_FINISH_UPDATE,"");
                            break;
                        case IvyConstants.UPDATE_TABLE_SUCCESS_CODE:
                            int updateTableCount = bundle.getInt("updateCount");
                            int totalTableCount = bundle.getInt("totalCount");
                            updaterProgressMsg(String.format(getResources().getString(R.string.table_downloaded), updateTableCount));
                            if (totalTableCount == (updateTableCount + 1)) {
                                updaterProgressMsg(getResources().getString(R.string.updating_tables));
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

        if (!bmodel.configurationMasterHelper.IS_RET_NAME_RETAILER_DOWNLOAD) {
            if (retailerList != null) {
                if (mSelectedLocBO != null) {
                    mRetailerListByLocOrUserId.put(mSelectedLocBO.getLocId(), retailerList);

                } else if (mSelectedUserBO != null) {
                    mRetailerListByLocOrUserId.put(mSelectedUserBO.getUserid(), retailerList);
                }
            }
        } else {
            mRetailerListByLocOrUserId.put(0, retailerList);
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
                holder.retailerNameTV = convertView.findViewById(R.id.tv_retailer_name);
                holder.retailerSelectCbox = convertView.findViewById(R.id.cb_retailer_select);
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
                holder.retailerNameTV = convertView.findViewById(R.id.tv_retailer_name);
                holder.retailerSelectCbox = convertView.findViewById(R.id.cb_retailer_select);
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
            if (bmodel.synchronizationHelper.getAuthErroCode().equals(IvyConstants.AUTHENTICATION_SUCCESS_CODE)) {
                if (!bmodel.configurationMasterHelper.IS_RET_NAME_RETAILER_DOWNLOAD) {
                    if (!bmodel.configurationMasterHelper.IS_USER_WISE_RETAILER_DOWNLOAD && mSelectedLocBO != null) {
                        bmodel.synchronizationHelper.downloadRetailerByLocFromServer(mSelectedLocBO.getLocId(), true);
                    } else {
                        if (mSelectedUserBO != null)
                            bmodel.synchronizationHelper.downloadRetailerByLocFromServer(mSelectedUserBO.getUserid(), false);
                    }
                } else {
                    bmodel.synchronizationHelper.downloadRetailerByRetailerName(mRetailerEdt.getText().toString().trim());
                }
            } else {
                String errorMsg = bmodel.synchronizationHelper.getErrormessageByErrorCode().get(bmodel.synchronizationHelper.getAuthErroCode());
                if (errorMsg != null) {
                    Toast.makeText(getActivity(), errorMsg, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.data_not_downloaded), Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

    class DownloadDataByRetailer extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            builder = new AlertDialog.Builder(getActivity());

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
            if (bmodel.synchronizationHelper.getAuthErroCode().equals(IvyConstants.AUTHENTICATION_SUCCESS_CODE)) {
                bool = false;
                ArrayList<RetailerMasterBO> retailerList = new ArrayList<>();
                retailerList.addAll(mSecondRetailerList);

                /*ArrayList<String> temp = new ArrayList<>();
                for (RetailerMasterBO bo : mSecondRetailerList) {
                    temp.add(bo.getRetailerID());
                }*/
                HashMap<String, Boolean> isRetailerExists = new HashMap<>();


                for (RetailerMasterBO bo : bmodel.getRetailerMaster()) {
                    isRetailerExists.put(bo.getRetailerID(), true);
                    if (bmodel.configurationMasterHelper.IS_DELETE_TABLE) {
                        if ((bmodel.configurationMasterHelper.SHOW_ALL_ROUTES || bo.getIsPlanned().equals("Y"))) {
                            retailerList.add(bo);
                        }
                    }
                }
                for (int i = 0; i < mSecondRetailerList.size(); i++) {
                    if (!isRetailerExists.containsKey(mSecondRetailerList.get(i).getRetailerID())) {
                        retailerList.add(mSecondRetailerList.get(i));
                    }
                }
                bmodel.synchronizationHelper.downloadMasterListBySelectedRetailer(retailerList, SynchronizationHelper.FROM_SCREEN.RETAILER_SELECTION);
            } else {
                String errorMsg = bmodel.synchronizationHelper.getErrormessageByErrorCode().get(bmodel.synchronizationHelper.getAuthErroCode());
                if (errorMsg != null) {
                    Toast.makeText(getActivity(), errorMsg, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.data_not_downloaded), Toast.LENGTH_SHORT).show();
                }
            }
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
