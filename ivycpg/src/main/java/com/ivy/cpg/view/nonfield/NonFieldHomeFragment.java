package com.ivy.cpg.view.nonfield;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
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

import com.ivy.cpg.view.attendance.AttendanceHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.cpg.view.homescreen.HomeScreenActivity;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FontUtils;

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
    ArrayList<NonFieldBO> nonFieldList = new ArrayList<>();
    CardView ll_title;
    MyAdapter mSchedule;
    private ArrayList<StandardListBO> childList;
    private int mSelectedIdIndex = -1;
    private String childUserName = "";
    private NonFieldHelper nonFieldHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nonfield, container, false);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        nonFieldHelper = NonFieldHelper.getInstance(getActivity());
        initializeItem();

        return view;
    }

    private void initializeItem() {


        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setIcon(null);
            actionBar.setTitle(null);
            actionBar.setElevation(0);
        }

        Bundle bundle = getArguments();
        if (bundle == null)
            bundle = getActivity().getIntent().getExtras();

        setScreenTitle(bundle.getString("screentitle"));

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
            lvList = getView().findViewById(R.id.listview);
            no_data_txt = getView().findViewById(R.id.no_data_txt);

            try {
                if (bmodel.labelsMasterHelper.applyLabels(no_data_txt.getTag()) != null)
                    no_data_txt.setText(bmodel.labelsMasterHelper.applyLabels(no_data_txt.getTag()));
            } catch (Exception e) {
                Commons.printException(e);
            }

            ll_title = getView().findViewById(R.id.card_title);
        }

        loadNonFieldDetails();
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_nonfield, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);


        menu.findItem(R.id.menu_select).setVisible(false);


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
            //select user
            showUserDialog();
            return true;
        } else if (i1 == R.id.menu_delete) {
            if (nonFieldHelper.hasDelete())
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
        childList = AttendanceHelper.getInstance(getActivity()).loadChildUserList(getActivity().getApplicationContext());
        if (childList != null && childList.size() > 0) {
            if (childList.size() > 1) {
                showDialog();
            } else if (childList.size() == 1) {
                mSelectedIdIndex = item;
                bmodel.setSelectedUserId(childList.get(0).getChildUserId());
                loadListData();
            }
        } else {
            bmodel.setSelectedUserId(bmodel.userMasterHelper.getUserMasterBO().getUserid());
            loadListData();
        }

    }

    private void showDialog() {
        ArrayAdapter<String> mChildUserNameAdapter = new ArrayAdapter<>(getActivity(),
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
                        loadListData();
                        dialog.dismiss();
                    }
                });

        AlertDialog objDialog = bmodel.applyAlertDialogTheme(builder);
        objDialog.setCancelable(false);
    }

    private void loadListData() {
        nonFieldHelper.downloadNonFieldDetails(getActivity().getApplicationContext());
        nonFieldList = nonFieldHelper.getNonFieldList();

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


        //if CNT01 is disabled
        loadListData();

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
        public @NonNull
        View getView(int position, View convertView, @NonNull ViewGroup parent) {
            final ViewHolder holder;
            final String PENDING = getResources().getString(R.string.pending);
            final String ACCEPTED = "Accepted";
            final String REJECTED = "Rejected";

            View row = convertView;
            if (row == null) {
                holder = new ViewHolder();

                LayoutInflater inflater = LayoutInflater.from(getActivity());
                final ViewGroup nullParent = null;
                row = inflater.inflate(R.layout.row_nonfield,
                        nullParent);

                holder.tvFromDatae = row
                        .findViewById(R.id.txt_frmDate);
                holder.tvToDate = row
                        .findViewById(R.id.txt_toDate);
                holder.tvSession = row
                        .findViewById(R.id.txt_session);
                holder.tvReason = row
                        .findViewById(R.id.txt_reason);
                holder.tvStatus = row
                        .findViewById(R.id.txt_descrp);

                holder.deleteCB = row
                        .findViewById(R.id.chk_delete);
                holder.tvTimeSpent = row.findViewById(R.id.txt_timespent);
                holder.tvUserName = row.findViewById(R.id.tvusername);
                holder.tvMonthName = row.findViewById(R.id.txt_monthName);
                holder.monthHeader = row.findViewById(R.id.month_header);
                holder.topLine = row.findViewById(R.id.top_line);

                holder.tvUserName.setTypeface(FontUtils.getFontBalooHai(getActivity(), FontUtils.FontType.REGULAR));

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
            holder.tvFromDatae.setText(DateTimeUtils.convertFromServerDateToRequestedFormat(holder.nonFieldBO.getFrmDate(),
                    ConfigurationMasterHelper.outDateFormat));
            holder.tvToDate.setText(DateTimeUtils.convertFromServerDateToRequestedFormat(holder.nonFieldBO.getToDate(),
                    ConfigurationMasterHelper.outDateFormat));
            holder.tvSession.setText(nonFieldHelper
                    .getSessionBOBySessionID(holder.nonFieldBO.getsessionID()));

            if (bmodel.configurationMasterHelper.IS_SHOW_DELETE_OPTION) {
                if (holder.nonFieldBO.getUpload().equalsIgnoreCase("Y")) {
                    holder.deleteCB.setVisibility(View.INVISIBLE);
                } else {
                    holder.deleteCB.setVisibility(View.VISIBLE);
                }
            }

            if (nonFieldHelper.getReasonBOByReasonID(holder.nonFieldBO.getReasonID()) != null)
                if ("LEAVE".equalsIgnoreCase(nonFieldHelper.getReasonBOByReasonID(holder.nonFieldBO.getReasonID())))
                    holder.tvReason.setText(nonFieldHelper.
                            getLeaveTypeByID(holder.nonFieldBO.getLeaveLovId()));
                else
                    holder.tvReason.setText(nonFieldHelper
                            .getReasonBOByReasonID(holder.nonFieldBO.getReasonID()));

            if ("R".equalsIgnoreCase(holder.nonFieldBO.getStatus())) {
                holder.tvStatus.setText(PENDING);
                holder.tvStatus.setTextColor(getResources().getColor(R.color.dark_red));
            } else if ("S".equalsIgnoreCase(holder.nonFieldBO.getStatus())) {
                holder.tvStatus.setText(ACCEPTED);
                holder.tvStatus.setTextColor(getResources().getColor(R.color.green_productivity));
            } else if ("D".equalsIgnoreCase(holder.nonFieldBO.getStatus())) {
                holder.tvStatus.setText(REJECTED);
                holder.tvStatus.setTextColor(getResources().getColor(R.color.date_picker_text_color));
            }

            if (nonFieldHelper.getReasonBOByReasonID(holder.nonFieldBO.getReasonID()) != null)
                if ("TRAVELTIME".equalsIgnoreCase(nonFieldHelper.getReasonBOByReasonID(holder.nonFieldBO.getReasonID()))) {
                    holder.tvTimeSpent.setText(holder.nonFieldBO.getTimeSpent());
                    holder.tvTimeSpent.setVisibility(View.VISIBLE);
                } else {
                    holder.tvTimeSpent.setText("");
                    holder.tvTimeSpent.setVisibility(View.GONE);
                }
            holder.tvUserName.setText(holder.nonFieldBO.getUserName());
            if (position % 2 == 1) {
                row.setBackgroundColor(getResources().getColor(R.color.white));
            } else {
                row.setBackgroundColor(getResources().getColor(R.color.divider_view_color));
            }

            if (holder.nonFieldBO.getMonthName().length() > 1) {
                holder.monthHeader.setVisibility(View.VISIBLE);
                String month_name = nonFieldHelper.changemonthName(holder.nonFieldBO.getMonthName());
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
        CheckBox deleteCB;
        ImageView topLine;
        TextView tvUserName;
    }

    class DeleteSelectedList extends AsyncTask<String, Integer, Boolean> {
        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        @Override
        protected Boolean doInBackground(String... arg0) {
            try {
                nonFieldHelper.deleteNonfield(getActivity().getApplicationContext());
                return Boolean.TRUE;
            } catch (Exception e) {
                Commons.printException(e);
                return Boolean.FALSE;
            }
        }

        protected void onPreExecute() {
            builder = new AlertDialog.Builder(getActivity());

            customProgressDialog(builder, getResources().getString(R.string.delete));
            alertDialog = builder.create();
            alertDialog.show();
        }

        protected void onPostExecute(Boolean result) {
            alertDialog.dismiss();
            loadNonFieldDetails();
        }
    }

}
