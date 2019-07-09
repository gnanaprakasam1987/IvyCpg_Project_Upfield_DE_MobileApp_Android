package com.ivy.cpg.primarysale.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.cpg.primarysale.bo.DistributorMasterBO;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.cpg.view.homescreen.HomeScreenActivity;
import com.ivy.utils.DateTimeUtils;

import java.util.ArrayList;

public class PrimarySaleFragment extends IvyBaseFragment {
    private BusinessModel bmodel;
    private ListView listView;
    private DistributorAdapter mSchedule;
    private Intent i;
    private boolean isClicked;
    private TypedArray typearr;
    private View view;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.primary_sale_fragment, container, false);

        typearr = getActivity().getTheme().obtainStyledAttributes(R.styleable.MyTextView);

        ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            // Set title to actionbar
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(bmodel.configurationMasterHelper.getPrimarysaleTitle());
            ((AppCompatActivity) getActivity()).getSupportActionBar().setIcon(null);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayUseLogoEnabled(false);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        listView = (ListView) view.findViewById(R.id.listView1);
        listView.setCacheColorHint(0);

        loadDistributorData();
        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:

                startActivity(new Intent(getActivity(),
                        HomeScreenActivity.class));
                getActivity().finish();
                return true;
        }
        return false;
    }

    public void loadDistributorData() {
        try {
            mSchedule = new DistributorAdapter(bmodel.distributorMasterHelper.getDistributors());
            listView.setAdapter(mSchedule);
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
    }

    private class DistributorAdapter extends ArrayAdapter<DistributorMasterBO> {

        ArrayList<DistributorMasterBO> distributorlistitems;

        private DistributorAdapter(ArrayList<DistributorMasterBO> distributorlistitems) {
            super(getActivity(), R.layout.activity_stockist_order_list_item, distributorlistitems);
            this.distributorlistitems = distributorlistitems;
        }


        public DistributorMasterBO getItem(int position) {
            return distributorlistitems.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return distributorlistitems.size();
        }

        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {

            final ViewHolder holder;
            final DistributorMasterBO distributorObj = distributorlistitems.get(position);

            if (convertView == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                convertView = inflater.inflate(
                        R.layout.activity_stockist_order_list_item, parent, false);
                holder = new ViewHolder();
                holder.outletNameTextView = (TextView) convertView.findViewById(R.id.outletName_tv);
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bmodel.distributorMasterHelper.setDistributor(distributorObj);
                        new DownloadProductAndPrice().execute();
                        i = new Intent(getActivity(), PrimarySaleHomeScreenActivity.class);
                    }

                });
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.outletNameTextView.setText(distributorObj.getDName());
            holder.outletNameTextView.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));


            if (position % 2 == 0)
                convertView.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_listcolor, 0));
            else
                convertView.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_listcolor_alt, 0));


            return convertView;
        }

        class ViewHolder {
            private TextView outletNameTextView;
        }
    }

    private class DownloadProductAndPrice extends
            AsyncTask<Integer, Integer, Boolean> {

        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        protected void onPreExecute() {

            builder = new AlertDialog.Builder(getActivity());

            customProgressDialog(builder, getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            return true;
        }

        protected void onPostExecute(Boolean result) {
            alertDialog.dismiss();
            String time = DateTimeUtils.now(DateTimeUtils.TIME);
            bmodel.distTimeStampHeaderHelper.setTimeIn(DateTimeUtils
                    .now(DateTimeUtils.DATE_GLOBAL) + " " + time);
            bmodel.distTimeStampHeaderHelper.saveTimeStamp(
                    DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL), time);
            startActivity(i);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbindDrawables(view.findViewById(R.id.root));
    }

    private void unbindDrawables(View view) {
        if (view != null) {
            if (view.getBackground() != null) {
                view.getBackground().setCallback(null);
            }
            if (view instanceof ViewGroup) {
                for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                    unbindDrawables(((ViewGroup) view).getChildAt(i));
                }
                try {
                    ((ViewGroup) view).removeAllViews();
                } catch (Exception e) {
                    Commons.printException("" + e);
                }
            }
        }
    }

}
