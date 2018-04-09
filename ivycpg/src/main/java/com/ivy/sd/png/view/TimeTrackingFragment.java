package com.ivy.sd.png.view;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.NonFieldTwoBo;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DateUtil;
import com.ivy.sd.png.view.InOutReasonDialog.OnMyDialogResult;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.StringTokenizer;

public class TimeTrackingFragment extends IvyBaseFragment {

    private BusinessModel bmodel;
    int addDialogrequestCode;
    private ListView listview;
    ArrayList<NonFieldTwoBo> nonFieldTwoBos = new ArrayList<NonFieldTwoBo>();
    MyAdapter mAdapter;
    private InOutReasonDialog dialog;
    OnMyDialogResult onmydailogresult;
    TextView no_data_txt;
    private AlertDialog objDialog = null;
    private boolean hide_selectuser_icon = false;
    private ArrayList<StandardListBO> childList;
    private ArrayAdapter<String> mChildUserNameAdapter;
    private int mSelectedIdIndex = -1;
    private String childUserName = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        View view = inflater.inflate(R.layout.fragment_time_tracking, container, false);
        listview = view.findViewById(R.id.listview);
        no_data_txt = view.findViewById(R.id.no_data_txt);

        //typeface
        no_data_txt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(null);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        setScreenTitle(bmodel.getMenuName("MENU_IN_OUT"));
        return view;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        mAdapter = new MyAdapter();
        listview.setAdapter(mAdapter);

        //condition to check CNT01
        if (bmodel.configurationMasterHelper.IS_CNT01) {
            bmodel.getCounterIdForUser();
            bmodel.setCounterId(bmodel.userMasterHelper.getUserMasterBO().getCounterId());
            //if CNT01 is enabled
            if (objDialog != null) {
                if (!objDialog.isShowing()) {
//                    showUserDialog();
                }
            } else {
                showUserDialog();
            }
        } else {
            //if CNT01 is disabled
            loadListData();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (bmodel.configurationMasterHelper.SHOW_CAPTURED_LOCATION) {
            int permissionStatus = ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                bmodel.locationUtil.startLocationListener();
            }
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        if (bmodel.configurationMasterHelper.SHOW_CAPTURED_LOCATION) {
            int permissionStatus = ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionStatus == PackageManager.PERMISSION_GRANTED)
                bmodel.locationUtil.stopLocationListener();
        }
    }

    public void loadNonFieldTwoDetails() {

        bmodel.mAttendanceHelper.downloadNonFieldTwoDetails();
        nonFieldTwoBos = bmodel.mAttendanceHelper.getNonFieldTwoBoList();
        mAdapter.notifyDataSetChanged();
    }

    private class MyAdapter extends ArrayAdapter<NonFieldTwoBo> {


        public MyAdapter() {
            super(getActivity(), R.layout.row_nonfield_two, nonFieldTwoBos);

        }

        public NonFieldTwoBo getItem(int position) {
            return nonFieldTwoBos.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return nonFieldTwoBos.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            if (convertView == null) {

                holder = new ViewHolder();

                LayoutInflater inflater = LayoutInflater.from(getActivity());
                convertView = (View) inflater.inflate(R.layout.row_nonfield_two,
                        null);

                holder.tvOutTime = (TextView) convertView
                        .findViewById(R.id.txt_fromTime);
                holder.btOutTime = (Button) convertView
                        .findViewById(R.id.btn_fromTime);
                holder.btInTime = (Button) convertView
                        .findViewById(R.id.btn_toTime);
                holder.tvInTime = (TextView) convertView
                        .findViewById(R.id.txt_toTime);
                holder.tvReason = (TextView) convertView
                        .findViewById(R.id.txt_reason);
                holder.tvStatus = (TextView) convertView
                        .findViewById(R.id.txt_status);

                //typefaces
                holder.tvOutTime.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.tvInTime.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.tvReason.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.tvStatus.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                ((TextView) convertView.findViewById(R.id.txt_Tit_To)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                ((TextView) convertView.findViewById(R.id.txt_Tit_from)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                ((TextView) convertView.findViewById(R.id.txt_Tit_Status)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                ((TextView) convertView.findViewById(R.id.txt_Tit_Reason)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.btOutTime.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
                holder.btInTime.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

                //holder.setTime = new SetTime(holder , getActivity());

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.nonFieldTwoBO = nonFieldTwoBos.get(position);
            holder.tvOutTime.setText(holder.nonFieldTwoBO.getOutTime());
            holder.tvStatus.setText(holder.nonFieldTwoBO.getStatus());
            holder.btOutTime.setText(getResources().getString(R.string.endC));
            String inTime = holder.nonFieldTwoBO.getInTime() != null ? holder.nonFieldTwoBO.getInTime() : " ";
            String outTime = holder.nonFieldTwoBO.getOutTime() != null ? holder.nonFieldTwoBO.getOutTime() : " ";
            String date = "";
            String time = "";
            StringTokenizer tokenizer;


            if (holder.nonFieldTwoBO.getOutTime() != null && !holder.nonFieldTwoBO.getOutTime().trim().equalsIgnoreCase("")) {
                holder.btOutTime.setVisibility(View.GONE);
                holder.tvOutTime.setVisibility(View.VISIBLE);
                tokenizer = new StringTokenizer(outTime);
                date = tokenizer.nextToken();
                time = tokenizer.nextToken();
                holder.tvOutTime.setText(DateUtil.convertFromServerDateToRequestedFormat(date,
                        ConfigurationMasterHelper.outDateFormat) + "\n" + time);
            } else {
                holder.tvOutTime.setVisibility(View.GONE);
                holder.btOutTime.setVisibility(View.VISIBLE);
                holder.btOutTime.setText(getResources().getString(R.string.endC));
            }


            if (holder.nonFieldTwoBO.getInTime() != null && !holder.nonFieldTwoBO.getInTime().trim().equalsIgnoreCase("")) {
                holder.btInTime.setVisibility(View.GONE);
                holder.tvInTime.setVisibility(View.VISIBLE);
                tokenizer = new StringTokenizer(inTime);
                date = tokenizer.nextToken();
                time = tokenizer.nextToken();
                holder.tvInTime.setText(DateUtil.convertFromServerDateToRequestedFormat(date,
                        ConfigurationMasterHelper.outDateFormat) + "\n" + time);
            } else {
                holder.tvInTime.setVisibility(View.GONE);
                holder.btInTime.setVisibility(View.VISIBLE);
                tokenizer = new StringTokenizer(inTime);
                date = tokenizer.nextToken();
                time = tokenizer.nextToken();
                holder.btInTime.setText(DateUtil.convertFromServerDateToRequestedFormat(date,
                        ConfigurationMasterHelper.outDateFormat) + "\n" + time);
            }

            holder.btInTime.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.nonFieldTwoBO.setInTime(SDUtil.now(SDUtil.DATE_TIME_NEW));
                    bmodel.mAttendanceHelper.updateNonFieldWorkTwoDetail(holder.nonFieldTwoBO);
                    loadNonFieldTwoDetails();
                }
            });

            holder.btOutTime.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.nonFieldTwoBO.setOutTime(SDUtil.now(SDUtil.DATE_TIME_NEW));
                    bmodel.mAttendanceHelper.updateNonFieldWorkTwoDetail(holder.nonFieldTwoBO);
                    loadNonFieldTwoDetails();
                }
            });

            holder.tvReason.setText(bmodel.mAttendanceHelper
                    .getReasonName(holder.nonFieldTwoBO.getReason()));

            return convertView;
        }

    }

    class ViewHolder {
        NonFieldTwoBo nonFieldTwoBO;
        TextView tvOutTime, tvReason, tvInTime, tvStatus;
        Button btInTime, btOutTime;
        CheckBox deleteCB;
        SetTime setTime;

    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_nonfield_two, menu);

    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        //condition to check CNT01
        if (bmodel.configurationMasterHelper.IS_CNT01) {
//            menu.findItem(R.id.menu_select).setVisible(true);
            if (hide_selectuser_icon) {
                menu.findItem(R.id.menu_select).setVisible(false);
            } else
                menu.findItem(R.id.menu_select).setVisible(true);
        } else {
            menu.findItem(R.id.menu_select).setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i1 = item.getItemId();
        if (i1 == android.R.id.home) {
            Intent i = new Intent(getActivity(), HomeScreenActivity.class);
            startActivity(i);
            getActivity().finish();
            return true;
        } else if (i1 == R.id.menu_select) {
            showUserDialog();
            return true;
        } else if (i1 == R.id.menu_add) {

            if (bmodel.mAttendanceHelper.previousInOutTimeCompleted()) {
                dialog = new InOutReasonDialog(getActivity(), onmydailogresult);
                dialog.setDialogResult(new InOutReasonDialog.OnMyDialogResult() {


                    public void cancel(String reasonid) {
                        dialog.dismiss();


                        NonFieldTwoBo addNonFieldTwoBo = new NonFieldTwoBo();
                        addNonFieldTwoBo.setId(bmodel.userMasterHelper.getUserMasterBO().getUserid()
                                + SDUtil.now(SDUtil.DATE_TIME_ID) + "");
                        addNonFieldTwoBo.setFromDate(SDUtil.now(SDUtil.DATE_GLOBAL));
                        addNonFieldTwoBo.setInTime(SDUtil.now(SDUtil.DATE_TIME_NEW));
                        addNonFieldTwoBo.setOutTime(null);
                        addNonFieldTwoBo.setRemarks("");
                        addNonFieldTwoBo.setReason(reasonid);
                        bmodel.mAttendanceHelper.saveNonFieldWorkTwoDetail(addNonFieldTwoBo);
                        if (bmodel.configurationMasterHelper.IS_IN_OUT_MANDATE) {
                            HomeScreenFragment.isLeave_today = bmodel.mAttendanceHelper.checkLeaveAttendance();
                        }


                        //}
                        listview.setVisibility(View.VISIBLE);
                        no_data_txt.setVisibility(View.GONE);
                        loadNonFieldTwoDetails();
                    }
                });
                dialog.show();

            } else {
                try {
                    bmodel.showAlert(getResources().getString(R.string.out_time_error), 0);
                } catch (Exception e) {
                    Commons.printException(e);
                }
            }

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
                        setScreenTitle(bmodel.getMenuName("MENU_IN_OUT") + " (" +
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
        bmodel.mAttendanceHelper.downloadNonFieldTwoDetails();
        nonFieldTwoBos = bmodel.mAttendanceHelper.getNonFieldTwoBoList();
        //data empty or not
        if (nonFieldTwoBos == null || !(nonFieldTwoBos.size() > 0)) {
            listview.setVisibility(View.GONE);
            no_data_txt.setVisibility(View.VISIBLE);
        } else {
            mAdapter.notifyDataSetChanged();
            listview.setVisibility(View.VISIBLE);
            no_data_txt.setVisibility(View.GONE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == addDialogrequestCode) {
            if (resultCode == Activity.RESULT_OK) {
                loadNonFieldTwoDetails();
            }
        }

    }


    // Time picker dialog on button click to register In Time
    class SetTime implements View.OnClickListener, TimePickerDialog.OnTimeSetListener {

        private ViewHolder holder;
        private Calendar myCalendar;
        private Context ctx;

        public SetTime(ViewHolder holder, Context ctx) {
            this.holder = holder;
            this.holder.btInTime.setOnClickListener(this);
            this.myCalendar = Calendar.getInstance();
            this.ctx = ctx;
        }

        @Override
        public void onClick(View v) {
            int hour = myCalendar.get(Calendar.HOUR_OF_DAY);
            int minute = myCalendar.get(Calendar.MINUTE);
            new TimePickerDialog(ctx, this, hour, minute, true).show();
        }


        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // TODO Auto-generated method stub

            DecimalFormat formatter = new DecimalFormat("00");
            this.holder.btInTime.setText(formatter.format(hourOfDay) + ":" + formatter.format(minute) + ":" + formatter.format(0));
            holder.nonFieldTwoBO.setInTime(formatter.format(hourOfDay) + ":" + formatter.format(minute) + ":" + formatter.format(0));
        }

    }


}
