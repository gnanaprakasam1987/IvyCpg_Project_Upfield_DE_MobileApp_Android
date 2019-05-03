package com.ivy.cpg.view.subd;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.core.IvyConstants;
import com.ivy.location.LocationUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.GenericObjectPair;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FontUtils;
import com.ivy.utils.StringUtils;

import java.util.Map;
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
        lvSubDId = view.findViewById(R.id.lv_subdid);

        setScreenTitle(bmodel.configurationMasterHelper.getSubdtitle());

        lvSubDId.setDivider(null);
        lvSubDId.setDividerHeight(0);
        if (retailer.size() > 0) {
            if (retailer.size() == 1) {
                bmodel.setRetailerMasterBO(retailer.get(0));
                loadHomeScreenTwo();
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

                holder.retailertNameTextView = convertView.findViewById(R.id.retailer_name_subdid);
                holder.cardViewItem = convertView.findViewById(R.id.cardview);
                holder.retailertNameTextView.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
                convertView.setTag(holder);

                holder.cardViewItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bmodel.setRetailerMasterBO(holder.retailerObjectHolder);
                        loadHomeScreenTwo();
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

    private void loadHomeScreenTwo() {

        // Time count Starts for the retailer
        if ((DateTimeUtils.compareDate(bmodel.userMasterHelper.getUserMasterBO()
                .getDownloadDate(), DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL), "yyyy/MM/dd") > 0)
                && bmodel.configurationMasterHelper.IS_DATE_VALIDATION_REQUIRED) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.next_day_coverage),
                    Toast.LENGTH_SHORT).show();

        } else {

            new DownloadProductsAndPrice().execute();
        }
    }


    @SuppressLint("StaticFieldLeak")
    private class DownloadProductsAndPrice extends AsyncTask<Integer, Integer, Boolean> {
        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {
                if (!isCancelled()) {
                    GenericObjectPair<Vector<ProductMasterBO>, Map<String, ProductMasterBO>> genericObjectPair = bmodel.productHelper.downloadProducts(MENU_STK_ORD);
                    if (genericObjectPair != null) {
                        bmodel.productHelper.setProductMaster(genericObjectPair.object1);
                        bmodel.productHelper.setProductMasterById(genericObjectPair.object2);
                    }
                    bmodel.productHelper.setFilterProductLevels(bmodel.productHelper.downloadFilterLevel(MENU_STK_ORD));
                    bmodel.productHelper.setFilterProductsByLevelId(bmodel.productHelper.downloadFilterLevelProducts(
                            bmodel.productHelper.getFilterProductLevels(), true));

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

                String date = DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL);
                String time = DateTimeUtils.now(DateTimeUtils.TIME);
                String temp = DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);

                String dateTime = date + " " + time;
                if (bmodel.configurationMasterHelper.IS_DISABLE_CALL_ANALYSIS_TIMER)
                    dateTime = IvyConstants.DEFAULT_TIME_CONSTANT;
                bmodel.outletTimeStampHelper.setTimeIn(dateTime);
                bmodel.outletTimeStampHelper.setUid(StringUtils.QT("OTS" + temp));

                bmodel.outletTimeStampHelper.saveTimeStamp(
                        DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL), time
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
