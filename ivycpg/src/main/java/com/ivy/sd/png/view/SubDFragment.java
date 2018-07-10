package com.ivy.sd.png.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.ivy.cpg.nfc.NFCManager;
import com.ivy.cpg.nfc.NFCReadDialogActivity;
import com.ivy.location.LocationUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.SupplierMasterBO;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.UserDialogInterface;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.TimerCount;
import com.ivy.sd.png.view.profile.ProfileActivity;

import java.util.ArrayList;
import java.util.Vector;


public class SubDFragment extends IvyBaseFragment {


    private static final String MENU_STK_ORD = "MENU_STK_ORD";

    BusinessModel bmodel;
    ListView lvSubDId;
    Context context;
    Vector<RetailerMasterBO> retailer = new Vector<>();
    RetailerSelectionAdapter adapter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sub_d, container, false);

        context = getActivity();
        retailer = bmodel.getSubDMaster();
        lvSubDId = (ListView) view.findViewById(R.id.lv_subdid);

        setScreenTitle(bmodel.configurationMasterHelper.getSubdtitle());

        lvSubDId.setDivider(null);
        lvSubDId.setDividerHeight(0);
        if (retailer.size() > 0) {
            if (retailer.size() == 1) {
                bmodel.setRetailerMasterBO(retailer.get(0));
                loadHomeScreenTwo(retailer.get(0));
            } else {
                adapter = new RetailerSelectionAdapter(retailer);
                lvSubDId.setAdapter(adapter);
            }
        }
        return view;
    }


    private class RetailerSelectionAdapter extends ArrayAdapter<RetailerMasterBO> {

        private final Vector<RetailerMasterBO> items;
        LayoutInflater inflater;
        private RetailerMasterBO retailerObj;

        private RetailerSelectionAdapter(Vector<RetailerMasterBO> items) {
            super(context, R.layout.visit_list_child_item, items);
            this.items = items;
            inflater = LayoutInflater.from(context);
        }

        public RetailerMasterBO getItem(int position) {
            return items.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return items.size();
        }

        @NonNull
        public View getView(final int position, View convertView, @NonNull ViewGroup parent) {

            final RetailerSelectionAdapter.ViewHolder holder;
            retailerObj = items.get(position);

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.row_subdid_layout, parent, false);
                holder = new RetailerSelectionAdapter.ViewHolder();

                holder.retailertNameTextView =  convertView.findViewById(R.id.retailer_name_subdid);
                holder.cardViewItem =  convertView.findViewById(R.id.cardview);
                holder.retailertNameTextView.setTypeface(bmodel.configurationMasterHelper
                        .getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                convertView.setTag(holder);

                holder.cardViewItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bmodel.setRetailerMasterBO(holder.retailerObjectHolder);
                        loadHomeScreenTwo(retailerObj);
                    }
                });

            } else {
                holder = (RetailerSelectionAdapter.ViewHolder) convertView.getTag();
            }

            holder.retailerObjectHolder = retailerObj;

            String tvText = items.get(position).getRetailerName();
            holder.retailertNameTextView.setText(tvText);
            return convertView;
        }

        class ViewHolder {
            private RetailerMasterBO retailerObjectHolder;
            private TextView retailertNameTextView;
            private CardView cardViewItem;

        }
    }

    private void loadHomeScreenTwo(RetailerMasterBO ret) {

        // Time count Starts for the retailer
        if ((SDUtil.compareDate(bmodel.userMasterHelper.getUserMasterBO()
                .getDownloadDate(), SDUtil.now(SDUtil.DATE_GLOBAL), "yyyy/MM/dd") > 0)
                && bmodel.configurationMasterHelper.IS_DATE_VALIDATION_REQUIRED) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.next_day_coverage),
                    Toast.LENGTH_SHORT).show();

        } else {

            new DownloadProductsAndPrice().execute();
        }
    }


    private class DownloadProductsAndPrice extends AsyncTask<Integer, Integer, Boolean> {
        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {
                if (!isCancelled()) {
                    if (!bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY) {
                        bmodel.productHelper
                                .downloadFiveFilterLevels(MENU_STK_ORD);
                        bmodel.productHelper
                                .downloadProductsWithFiveLevelFilter(MENU_STK_ORD);
                    } else if (bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY) {
                        //to reload product filter if diffrent retailer selected
                        bmodel.productHelper.setmLoadedGlobalProductId(0);
                    }
                    bmodel.configurationMasterHelper
                            .loadOrderAndStockConfiguration(bmodel.retailerMasterBO
                                    .getSubchannelid());

                    //Getting Attributes mapped for the retailer
                    bmodel.getAttributeHierarchyForRetailer();

                }
            } catch (Exception e) {
                Commons.printException("" + e);
            }
            return Boolean.TRUE;
        }

        protected void onPreExecute() {
            if (!isCancelled()) {
                builder = new AlertDialog.Builder(getActivity());
                customProgressDialog(builder, getResources().getString(R.string.loading));
                alertDialog = builder.create();
                alertDialog.show();
            }
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(Boolean result) {
            if (!isCancelled()) {

                String date = SDUtil.now(SDUtil.DATE_GLOBAL);
                String time = SDUtil.now(SDUtil.TIME);
                String temp = SDUtil.now(SDUtil.DATE_TIME_ID);

                bmodel.outletTimeStampHelper.setTimeIn(date + " " + time);
                bmodel.outletTimeStampHelper.setUid(bmodel.QT("OTS" + temp));

                bmodel.outletTimeStampHelper.saveTimeStamp(
                        SDUtil.now(SDUtil.DATE_GLOBAL), time
                        , 0, "", "", "", "0");

                if (alertDialog != null && alertDialog.isShowing()) {
                    alertDialog.dismiss();
                }
                //set selected retailer location and its used on retailer modules
                bmodel.mSelectedRetailerLatitude = LocationUtil.latitude;
                bmodel.mSelectedRetailerLongitude = LocationUtil.longitude;

                Intent i = new Intent(getActivity(), SubDHomeActivity.class);
                startActivity(i);
            }
        }
    }

}
