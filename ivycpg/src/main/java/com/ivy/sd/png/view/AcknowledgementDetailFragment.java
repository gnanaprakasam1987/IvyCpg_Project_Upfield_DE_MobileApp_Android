package com.ivy.sd.png.view;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.JointCallAcknowledgementBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.DateUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anandasir.v on 9/7/2017.
 */

public class AcknowledgementDetailFragment extends IvyBaseFragment {
    BusinessModel bmodel;
    View view;
    FragmentManager fm;
    String userID = "", screenTitle = "";
    private ArrayList<JointCallAcknowledgementBO> joinCallAcknowledgementList;
    RecyclerView dashBoardList;
    DashBoardListViewAdapter dashBoardListViewAdapter;
    private ActionBar actionBar;
    private boolean isChecked = false;
    TextView txtUser;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_acknowledgementdetail, container, false);

        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        fm = getActivity().getSupportFragmentManager();
        if (getActivity().getIntent().getExtras() != null) {
            userID = getActivity().getIntent().getStringExtra("UserID");
            screenTitle = getActivity().getIntent().getStringExtra("screentitle");
        }
        setHasOptionsMenu(true);
//        setUpActionBar();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setElevation(0);
            setScreenTitle(screenTitle);
        }
        txtUser = (TextView) view.findViewById(R.id.txtUser);

        //chkAll = (CheckBox) view.findViewById(R.id.img_checkAll);
        dashBoardList = (RecyclerView) view.findViewById(R.id.acknowledgementDetailLV);
        dashBoardList.setHasFixedSize(false);
        dashBoardList.setNestedScrollingEnabled(false);
        dashBoardList.setLayoutManager(new LinearLayoutManager(getActivity()));

        bmodel.acknowledgeHelper.loadJointCallAcknowledgement(userID);
        joinCallAcknowledgementList = bmodel.acknowledgeHelper.getAcknowledgementList();

        dashBoardListViewAdapter = new DashBoardListViewAdapter(joinCallAcknowledgementList);
        dashBoardList.setAdapter(dashBoardListViewAdapter);
    }

    public class DashBoardListViewAdapter extends RecyclerView.Adapter<DashBoardListViewAdapter.ViewHolder> {
        private final List<JointCallAcknowledgementBO> dashboardList;

        public DashBoardListViewAdapter(List<JointCallAcknowledgementBO> dashboardList) {
            this.dashboardList = dashboardList;
        }

        @Override
        public DashBoardListViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_acknowledgement_detail, parent, false);
            return new DashBoardListViewAdapter.ViewHolder(v);
        }


        @Override
        public void onBindViewHolder(final DashBoardListViewAdapter.ViewHolder holder, int position) {
            final JointCallAcknowledgementBO dashboardData = dashboardList.get(position);

            holder.invoiceHeaderBO = joinCallAcknowledgementList.get(position);
            txtUser.setText(holder.invoiceHeaderBO.getUsername());
            //holder.txtUserName.setText(holder.invoiceHeaderBO.getUsername());
            holder.txtDate.setText(DateUtil.convertFromServerDateToRequestedFormat(holder.invoiceHeaderBO.getDate(),
                    ConfigurationMasterHelper.outDateFormat));
            holder.txtBeat.setText(holder.invoiceHeaderBO.getBeat());
            holder.txtValue.setText(holder.invoiceHeaderBO.getValue());
            holder.txtRetailer.setText(holder.invoiceHeaderBO.getRetailer());
            //typefaces
            //holder.txtUserName.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
            holder.txtDate.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.txtBeat.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
            holder.txtValue.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.txtRetailer.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

            holder.imgInvSelected.setChecked(holder.invoiceHeaderBO.getUpload().equals("N"));


            //for P3M trend Chart loading
            holder.chkLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder.imgInvSelected.isChecked()) {
                        holder.imgInvSelected.setChecked(false);
                    } else {
                        holder.imgInvSelected.setChecked(true);
                    }
                }
            });

            holder.imgInvSelected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        holder.invoiceHeaderBO.setUpload("N");
                    } else {
                        holder.invoiceHeaderBO.setUpload("Y");
                    }

                }
            });
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return dashboardList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            //TextView txtUserName;
            TextView txtDate;
            TextView txtBeat;
            TextView txtRetailer;
            TextView txtValue;
            CheckBox imgInvSelected;
            LinearLayout chkLayout;
            JointCallAcknowledgementBO invoiceHeaderBO;

            public ViewHolder(View row) {
                super(row);
                //txtUserName = (TextView) row.findViewById(R.id.txtUserName);
                txtDate = (TextView) row.findViewById(R.id.txtDate);
                txtBeat = (TextView) row.findViewById(R.id.txtBeat);
                txtRetailer = (TextView) row.findViewById(R.id.txtRetailer);
                txtValue = (TextView) row.findViewById(R.id.txtValue);
                chkLayout = (LinearLayout) row.findViewById(R.id.chkLayout);
                //holder.imgInvSelected = (ImageView) row.findViewById(R.id.img_check);
                imgInvSelected = (CheckBox) row.findViewById(R.id.img_check);
            }
        }
    }

    private void setUpActionBar() {
        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (bmodel.getMenuName("MENU_DASH").endsWith(""))
            bmodel.configurationMasterHelper.downloadMainMenu();
        if (getArguments().getString("screentitle") == null)
            actionBar.setTitle(bmodel.getMenuName("MENU_DASH"));
        else
            actionBar.setTitle(getArguments().getString("screentitle"));
        actionBar.setIcon(R.drawable.icon_visit);

        if (!BusinessModel.dashHomeStatic)
            actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.menu_acknowledgement, menu);
        super.onCreateOptionsMenu(menu, inflater);

//        chkAll = (CheckBox) menu.findItem(R.id.chkall).getActionView();
//        chkAll.setChecked(false);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        //menu.findItem(R.id.search).setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            Intent intent = new Intent(getActivity(),
                    AcknowledgementActivity.class);
            intent.putExtra("screentitle", screenTitle);
            startActivity(intent);
            getActivity().finish();
            return true;
        } else if (i == R.id.menu_save) {
            new SaveAsyncTask().execute();
            return true;
        } else if(i == R.id.chkall){
            isChecked = !isChecked;
            item.setIcon(isChecked ? R.drawable.chkall: R.drawable.chkall_unchecked);
            String Status = (isChecked) ? "N" : "Y";
            for (JointCallAcknowledgementBO jointCall : joinCallAcknowledgementList) {
                jointCall.setUpload(Status);
            }
            dashBoardListViewAdapter.notifyDataSetChanged();
        }
        return super.onOptionsItemSelected(item);
    }

    class SaveAsyncTask extends AsyncTask<Void, Integer, Boolean> {

        private ProgressDialog progressDialogue;
        boolean result = true;

        @Override
        protected Boolean doInBackground(Void... arg0) {
            try {
                for (JointCallAcknowledgementBO acknowledgeBO : joinCallAcknowledgementList) {
                    bmodel.acknowledgeHelper.updateAcknowledgement(acknowledgeBO.getUserid(),
                            acknowledgeBO.getRefid(), acknowledgeBO.getUpload());
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return Boolean.FALSE;
            }
        }

        protected void onPreExecute() {
            progressDialogue = ProgressDialog.show(getActivity(), "",
                    "Saving Details...", true, false);
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {
            // result is the value returned from doInBackground
            progressDialogue.dismiss();
            if (result) {
                Intent intent = new Intent(getActivity(),
                        AcknowledgementActivity.class);
                intent.putExtra("screentitle", screenTitle);
                startActivity(intent);
                getActivity().finish();
            }
        }
    }
}
