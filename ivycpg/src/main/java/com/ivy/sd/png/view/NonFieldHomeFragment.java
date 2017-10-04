package com.ivy.sd.png.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.NonFieldBO;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DateUtil;

import java.util.ArrayList;

import static com.ivy.sd.png.asean.view.R.id.menu_delete;
import static com.ivy.sd.png.asean.view.R.string.item;

/**
 * Created by subramanian on 4/12/17.
 */

public class NonFieldHomeFragment extends IvyBaseFragment {

    private BusinessModel bmodel;
    private ListView lvList;
    TextView no_data_txt;
    private ActionBar actionBar;
    ArrayList<NonFieldBO> nonFieldList = new ArrayList<>();
    CardView ll_title;
    MyAdapter mSchedule;
    private AlertDialog objDialog = null;
    private boolean hide_selectuser_icon = false;
    private ArrayList<StandardListBO> childList;
    private ArrayAdapter<String> mChildUserNameAdapter;
    private int mSelectedIdIndex = -1;
    private String childUserName = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nonfield, container, false);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        initializeItem();

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
   /*     if (toolbar != null) {
            ((AppCompatActivity)getActivity()).getSupportActionBar().hide();

            ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
            ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(null);
            TextView toolBarTitle = (TextView)view.findViewById(R.id.tv_toolbar_title);
            toolBarTitle.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
            toolBarTitle.setText(bmodel.configurationMasterHelper.getTradecoveragetitle());
            ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
            ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(true);
            ((AppCompatActivity)getActivity()).getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_navigation_drawer);
            ((AppCompatActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
            ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayUseLogoEnabled(true);
        }
*/
        return view;
    }

    private void initializeItem() {


        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setIcon(null);
            actionBar.setTitle(null);
            actionBar.setElevation(0);
            //  actionBar.setStackedBackgroundDrawable((new ColorDrawable(ContextCompat.getColor(getActivity(),R.color.toolbar_ret_bg))));
        }

        setScreenTitle(bmodel.configurationMasterHelper.getTradecoveragetitle());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getView() != null) {
            lvList = (ListView) getView().findViewById(R.id.listview);
            no_data_txt = (TextView) getView().findViewById(R.id.no_data_txt);
            ll_title = (CardView) getView().findViewById(R.id.card_title);
        }

        loadNonFieldDetails();
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_nonfield, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (bmodel.configurationMasterHelper.IS_CNT01) {
            if (hide_selectuser_icon) {
                menu.findItem(R.id.menu_select).setVisible(false);
            } else
                menu.findItem(R.id.menu_select).setVisible(true);
        } else {
            menu.findItem(R.id.menu_select).setVisible(false);
        }

        if (bmodel.configurationMasterHelper.IS_SHOW_DELETE_OPTION)
            menu.findItem(menu_delete).setVisible(true);
        else
            menu.findItem(menu_delete).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i1 = item.getItemId();
        if (i1 == android.R.id.home) {
            Intent i = new Intent(getActivity(), HomeScreenActivity.class);
            startActivity(i);
            getActivity().finish();
            return true;
        } else if (i1 == R.id.menu_add) {
            Intent i = new Intent(getActivity(), NonFieldFragment.class);
            startActivity(i);

            return true;
        } else if (i1 == R.id.menu_select) {
            //bmodel.mAttendanceHelper.saveNonFieldWorkTwoDetails(nonFieldTwoBos);

            //select user
            showUserDialog();
            return true;
        } else if (i1 == R.id.menu_delete) {
            if (bmodel.mAttendanceHelper.hasDelete())
                new DeleteSelectedList().execute();
            else
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.no_data_to_delete),
                        Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showUserDialog() {
        childList = bmodel.mAttendanceHelper.loadChildUserList();
        if (childList != null && childList.size() > 0) {
            if (childList.size() > 1) {
                showDialog();
            } else if (childList.size() == 1) {
                hide_selectuser_icon = true;
                mSelectedIdIndex = item;
                bmodel.setSelectedUserId(childList.get(0).getChildUserId());
                loadListData();
            }
        } else {
            hide_selectuser_icon = true;
            bmodel.setSelectedUserId(bmodel.userMasterHelper.getUserMasterBO().getUserid());
            loadListData();
        }

    }

    private void showDialog() {
        mChildUserNameAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.select_dialog_singlechoice);

        for (StandardListBO temp : childList)
            mChildUserNameAdapter.add(temp.getChildUserName());

        AlertDialog.Builder builder;

        builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select User");
        builder.setSingleChoiceItems(mChildUserNameAdapter, mSelectedIdIndex,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        mSelectedIdIndex = item;
                        bmodel.setSelectedUserId(childList.get(item).getChildUserId());
                        childUserName = childList.get(item).getChildUserName();
                        setScreenTitle(bmodel.configurationMasterHelper.getTradecoveragetitle() + " (" +
                                childUserName + ")");
                        hide_selectuser_icon = false;
                        loadListData();
                        dialog.dismiss();
                    }
                });

        objDialog = bmodel.applyAlertDialogTheme(builder);
        objDialog.setCancelable(false);
    }

    private void loadListData() {
        bmodel.mAttendanceHelper.downloadNonFieldDetails();
        nonFieldList = bmodel.mAttendanceHelper.getNonFieldList();

        //data empty or not
        if (nonFieldList == null || !(nonFieldList.size() > 0)) {
            ll_title.setVisibility(View.GONE);
            no_data_txt.setVisibility(View.VISIBLE);
        } else {
            mSchedule.notifyDataSetChanged();
            ll_title.setVisibility(View.VISIBLE);
            no_data_txt.setVisibility(View.GONE);
        }
    }

    private void loadNonFieldDetails() {

        mSchedule = new MyAdapter();
        lvList.setAdapter(mSchedule);

//condition to check CNT01
        if (bmodel.configurationMasterHelper.IS_CNT01) {
            //if CNT01 is enabled
            if (objDialog != null) {
                if (!objDialog.isShowing()) {
                    loadListData();
                }
            } else {
                showUserDialog();
            }
        } else {
            //if CNT01 is disabled
            loadListData();
        }
    }

    private final class MyAdapter extends ArrayAdapter<NonFieldBO> {

        public MyAdapter() {
            super(getActivity(), R.layout.row_nonfield, nonFieldList);
        }

        public NonFieldBO getItem(int position) {
            return nonFieldList.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return nonFieldList.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            final String PENDING = "Pending";
            final String ACCEPTED = "Accepted";
            final String REJECTED = "Rejected";

            View row = convertView;
            if (row == null) {
                holder = new ViewHolder();

                LayoutInflater inflater = LayoutInflater.from(getActivity());
                final ViewGroup nullParent = null;
                row = inflater.inflate(R.layout.row_nonfield,
                        nullParent);

                holder.tvFromDatae = (TextView) row
                        .findViewById(R.id.txt_frmDate);
                holder.tvToDate = (TextView) row
                        .findViewById(R.id.txt_toDate);
                holder.tvSession = (TextView) row
                        .findViewById(R.id.txt_session);
                holder.tvReason = (TextView) row
                        .findViewById(R.id.txt_reason);
                holder.tvStatus = (TextView) row
                        .findViewById(R.id.txt_descrp);

                holder.deleteCB = (CheckBox) row
                        .findViewById(R.id.chk_delete);
                holder.tvTimeSpent = (TextView) row.findViewById(R.id.txt_timespent);
                holder.tvMonthName = (TextView) row.findViewById(R.id.txt_monthName);
                holder.monthHeader = (LinearLayout) row.findViewById(R.id.month_header);
                holder.topLine = (ImageView) row.findViewById(R.id.top_line);
                if (bmodel.configurationMasterHelper.IS_SHOW_DELETE_OPTION)
                    holder.deleteCB.setVisibility(View.VISIBLE);
                else
                    holder.deleteCB.setVisibility(View.GONE);


                holder.deleteCB.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (((CheckBox) v).isChecked())
                            holder.nonFieldBO.setDeleteRequest(true);
                        else
                            holder.nonFieldBO.setDeleteRequest(false);
                    }
                });
                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            holder.nonFieldBO = nonFieldList.get(position);
            holder.tvFromDatae.setText(DateUtil.convertFromServerDateToRequestedFormat(holder.nonFieldBO.getFrmDate(),
                    ConfigurationMasterHelper.outDateFormat));
            holder.tvToDate.setText(DateUtil.convertFromServerDateToRequestedFormat(holder.nonFieldBO.getToDate(),
                    ConfigurationMasterHelper.outDateFormat));
            holder.tvSession.setText(bmodel.mAttendanceHelper
                    .getSessionBOBySessionID(holder.nonFieldBO.getsessionID()));

            if (bmodel.configurationMasterHelper.IS_SHOW_DELETE_OPTION) {
                if (holder.nonFieldBO.getUpload().equalsIgnoreCase("Y")) {
                    holder.deleteCB.setVisibility(View.INVISIBLE);
                } else {
                    holder.deleteCB.setVisibility(View.VISIBLE);
                }
            }

            if (bmodel.mAttendanceHelper.getReasonBOByReasonID(holder.nonFieldBO.getReasonID()) != null)
                if ("LEAVE".equalsIgnoreCase(bmodel.mAttendanceHelper.getReasonBOByReasonID(holder.nonFieldBO.getReasonID())))
                    holder.tvReason.setText(bmodel.mAttendanceHelper.
                            getLeaveTypeByID(holder.nonFieldBO.getLeaveLovId()));
                else
                    holder.tvReason.setText(bmodel.mAttendanceHelper
                            .getReasonBOByReasonID(holder.nonFieldBO.getReasonID()));

            if ("R".equalsIgnoreCase(holder.nonFieldBO.getStatus())) {
                holder.tvStatus.setText(PENDING);
                holder.tvStatus.setTextColor(getResources().getColor(R.color.dark_red));
            } else if ("S".equalsIgnoreCase(holder.nonFieldBO.getStatus())) {
                holder.tvStatus.setText(ACCEPTED);
                holder.tvStatus.setTextColor(getResources().getColor(R.color.btn_round_green));
            } else if ("D".equalsIgnoreCase(holder.nonFieldBO.getStatus())) {
                holder.tvStatus.setText(REJECTED);
            }

            if (bmodel.mAttendanceHelper.getReasonBOByReasonID(holder.nonFieldBO.getReasonID()) != null)
                if ("TRAVELTIME".equalsIgnoreCase(bmodel.mAttendanceHelper.getReasonBOByReasonID(holder.nonFieldBO.getReasonID()))) {
                    holder.tvTimeSpent.setText(holder.nonFieldBO.getTimeSpent());
                    holder.tvTimeSpent.setVisibility(View.VISIBLE);
                } else {
                    holder.tvTimeSpent.setText("");
                    holder.tvTimeSpent.setVisibility(View.GONE);
                }

            if (position % 2 == 1) {
                row.setBackgroundColor(getResources().getColor(R.color.white));
            } else {
                row.setBackgroundColor(getResources().getColor(R.color.white_smoke_color));
            }

            if (holder.nonFieldBO.getMonthName().length() > 1) {
                holder.monthHeader.setVisibility(View.VISIBLE);
                String month_name = bmodel.mAttendanceHelper.changemonthName(holder.nonFieldBO.getMonthName());
                holder.tvMonthName.setText(month_name);
                if (!month_name.equalsIgnoreCase("THIS MONTH")) {
                    holder.topLine.setVisibility(View.VISIBLE);
                } else {
                    holder.topLine.setVisibility(View.GONE);
                }
                holder.monthHeader.setBackgroundColor(getResources().getColor(R.color.white));
            } else {
                holder.monthHeader.setVisibility(View.GONE);
            }

            return row;
        }
    }

    class ViewHolder {
        NonFieldBO nonFieldBO;
        TextView tvFromDatae;
        TextView tvToDate;
        TextView tvSession;
        TextView tvReason;
        TextView tvStatus;
        TextView tvTimeSpent;
        TextView tvMonthName;
        LinearLayout monthHeader;
        LinearLayout llrow;
        CheckBox deleteCB;
        ImageView topLine;
    }

    class DeleteSelectedList extends AsyncTask<String, Integer, Boolean> {
        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        @Override
        protected Boolean doInBackground(String... arg0) {
            try {
                bmodel.mAttendanceHelper.deleteNonfield();
                return Boolean.TRUE;
            } catch (Exception e) {
                Commons.printException(e);
                return Boolean.FALSE;
            }
        }

        protected void onPreExecute() {
            builder = new AlertDialog.Builder(getActivity());

            bmodel.customProgressDialog(alertDialog, builder, getActivity(), getResources().getString(R.string.delete));
            alertDialog = builder.create();
            alertDialog.show();
        }

        protected void onPostExecute(Boolean result) {
            alertDialog.dismiss();
            loadNonFieldDetails();
        }
    }

}
