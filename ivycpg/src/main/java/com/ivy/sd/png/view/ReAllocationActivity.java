package com.ivy.sd.png.view;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.ivy.cpg.view.homescreen.HomeScreenActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.TaskAssignBO;
import com.ivy.sd.png.bo.TeamLeadBO;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.MyThread;
import com.ivy.sd.png.model.RemoveRetailerInterface;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by rajesh.k on 27-06-2016.
 */
@SuppressLint("NewApi")
public class ReAllocationActivity extends IvyBaseActivityNoActionBar implements
        View.OnDragListener, RemoveRetailerInterface {
    private static final String TAG = "ReAllocationActivity";
    private BusinessModel bmodel;
    private ListView mAbsenteesRetailersLV, mPresentRetailerAllocLV;
    private Spinner mAbsenteesSpin;
    private ArrayAdapter mAbsenteesAdapter;

    private ArrayList<String> mPresentIDList = new ArrayList<String>();
    private ArrayList<TeamLeadBO> mAbsenteIDList = new ArrayList<TeamLeadBO>();
    private ArrayList<TaskAssignBO> mAbsenteesList = new ArrayList<TaskAssignBO>();
    private ArrayList<TaskAssignBO> mTemperorayPresentList = new ArrayList<TaskAssignBO>();
    private ArrayList<TaskAssignBO> mSelectedAbsenteesList = new ArrayList<TaskAssignBO>();
    private HashMap<String, ArrayList<TaskAssignBO>> mPresentTaskListByMerchandiserID = new HashMap<>();
    private HashMap<String, ArrayList<TaskAssignBO>> mAbsenteesTaskListByMerchandiserID;
    private AbsenteesRetailerAdapter retailerAdapter;
    private TaskAssignBO mSelectedDropBO;
    private int mSelectedDropPos;
    private int mSelectedGroupPos;
    private int mSelectedChildPos;
    private String mSelecteAbsenteesID;
    private View mSelectedAbsenteesView;

    private int[] mEndPosition = new int[2];
    private ViewHolder mViewHolder;
    private AlertDialog.Builder builder;
    private AlertDialog alertDialog;
    private boolean isClicked = false;
    private Toolbar toolbar;


    private Animation fab_open, fab_close, rotate_forward, rotate_backward;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_assign);

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        bmodel = (BusinessModel) this.getApplicationContext();
        bmodel.setContext(this);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(bmodel.mSelectedActivityName);
            getSupportActionBar().setIcon(R.drawable.icon_visit);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
          //  getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        }


        mAbsenteesRetailersLV = (ListView) findViewById(R.id.lv_absentees_retailers);
        mPresentRetailerAllocLV = (ListView) findViewById(R.id.lv_allocate_retailer);
        mAbsenteesSpin = (Spinner) findViewById(R.id.spin_absent_user);


        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_backward);

        mAbsenteesSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TeamLeadBO teamLeadBO = (TeamLeadBO) mAbsenteesSpin.getSelectedItem();

                updateList(teamLeadBO.getUserID());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        mAbsenteesRetailersLV.setOnDragListener(this);
        mPresentRetailerAllocLV.setOnDragListener(this);


        loadData();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_only_next, menu);

        return super.onCreateOptionsMenu(menu);
    }

    public void loadData() {
        bmodel.teamLeadermasterHelper.downloadAbsenteesMerchandiser();
        bmodel.teamLeadermasterHelper.downloadMerchandiser();
       /* bmodel.teamLeadermasterHelper.downloadPresentMerchandiser();
        bmodel.teamLeadermasterHelper.downloadCompanyMaster();
        bmodel.teamLeadermasterHelper.downloadMerchandiser();*/


        mAbsenteesList = bmodel.teamLeadermasterHelper.getAbsenteesList();

        mAbsenteesTaskListByMerchandiserID = bmodel.teamLeadermasterHelper
                .getAbsentTaskListByMerchandiserID();
        // updateList(0 + "");


        final ArrayList<TeamLeadBO> userList = bmodel.teamLeadermasterHelper.getUserList();
        for (TeamLeadBO teamLeadBO : userList) {
            if (teamLeadBO.getAttendance() == 1) {
                mPresentIDList.add(teamLeadBO.getUserID());
            } else {
                mAbsenteIDList.add(teamLeadBO);
            }
        }

        mAbsenteesAdapter = new ArrayAdapter<TeamLeadBO>(
                ReAllocationActivity.this, android.R.layout.simple_spinner_item, mAbsenteIDList);
        mAbsenteesAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        mAbsenteesSpin.setAdapter(mAbsenteesAdapter);


        updatePresentRetailerHashMap();


        updateAdapter();

    }

    @Override
    public void removeRetailer(ArrayList<TaskAssignBO> retailerList, String userid) {

        Iterator itr = retailerList.iterator();
        while (itr.hasNext()) {
            TaskAssignBO taskAssignBO = (TaskAssignBO) itr.next();
            if (taskAssignBO.isChecked()) {
                addItem(taskAssignBO);
                itr.remove();
            }
        }


        mTemperorayPresentList = retailerList;
        mPresentTaskListByMerchandiserID.put(userid, mTemperorayPresentList);


        if (mSelecteAbsenteesID != null) {

            updateList(mSelecteAbsenteesID);
        } else {
            updateList(0 + "");
        }

        updateAdapter();
    }


    @SuppressLint("NewApi")
    class AbsenteesRetailerAdapter extends BaseAdapter {
        private ArrayList<TaskAssignBO> absenteesList;

        public AbsenteesRetailerAdapter(ArrayList<TaskAssignBO> absentees) {
            // TODO Auto-generated constructor stub
            super();
            this.absenteesList = absentees;

        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return absenteesList.size();
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
            final ViewHolder holder;
            if (convertView == null) {
                LayoutInflater inflater = getLayoutInflater();
                convertView = inflater.inflate(R.layout.list_task_assign,
                        parent, false);
                holder = new ViewHolder();
                holder.RetailerTV = (TextView) convertView
                        .findViewById(R.id.tv_retailer_name);


                convertView.setOnLongClickListener(new View.OnLongClickListener() {

                    @Override
                    public boolean onLongClick(View v) {
                        holder.RetailerTV.setTextColor(getResources().getColor(
                                android.R.color.holo_red_light));

                        v.setBackgroundColor(getResources().getColor(
                                R.color.white));

                        mSelectedDropPos = holder.pos;
                        startDrag(v, holder.taskAssignBO);
                        return true;
                    }
                });

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.view = convertView;
            holder.taskAssignBO = absenteesList.get(position);
            holder.pos = position;


            holder.RetailerTV.setText(holder.taskAssignBO.getRetailerName());


            // TODO Auto-generated method stub
            return convertView;
        }
    }


    class ViewHolder {
        TaskAssignBO taskAssignBO;
        View view;
        int pos;
        TextView RetailerTV;
        TextView clientTV;
        ImageView userIMG;
        TextView userTV;
        TextView retailerCountTV;
        LinearLayout retailerListLL, presentNameLL;
        FrameLayout retailerCountFL;
        String presentUserID;
        FloatingActionButton retailerCountBTN;
        ArrayList<TaskAssignBO> retailerList;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Intent i = new Intent(ReAllocationActivity.this, HomeScreenActivity.class);
            startActivity(i);
            finish();
            return true;
        } else if (id == R.id.menu_next) {
            if (!isClicked)
                nextButtonClick();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void nextButtonClick() {
        isClicked = true;
        builder = new AlertDialog.Builder(ReAllocationActivity.this);

        customProgressDialog(builder, getResources().getString(R.string.uploading_data));
        alertDialog = builder.create();
        alertDialog.show();
        bmodel.teamLeadermasterHelper.saveReAllocation(mPresentIDList, mPresentTaskListByMerchandiserID);
        new MyThread(this, DataMembers.SYNC_REALLOC_UPLOAD).start();
    }

    @Override
    public boolean onDrag(View v, DragEvent ev) {
        // TODO Auto-generated method stub
        final int action = ev.getAction();
        final int x = (int) ev.getX();
        final int y = (int) ev.getY();
        long packagedPosition = 0;
        int flatPosition = 0;
        flatPosition = mPresentRetailerAllocLV.pointToPosition(x, y);

        packagedPosition = mPresentRetailerAllocLV
                .getItemIdAtPosition(flatPosition);
        switch (action) {
            case DragEvent.ACTION_DRAG_STARTED:

                return true;
            case DragEvent.ACTION_DRAG_ENTERED:

                return true;

            case DragEvent.ACTION_DROP:


                mTemperorayPresentList = new ArrayList<TaskAssignBO>();
            /*
			 * check dropped view is equal to present merchandiser expandable
			 * listview
			 */
                if (v == mPresentRetailerAllocLV) {
                    mEndPosition[0] = flatPosition;

                    if (mEndPosition[0] != -1) {

                        UserMasterBO userBo = bmodel.teamLeadermasterHelper
                                .getMerchandiserBOByUserID().get(
                                        mPresentIDList.get(mEndPosition[0]));

                        mTemperorayPresentList = mPresentTaskListByMerchandiserID
                                .get(mPresentIDList.get(mEndPosition[0]));
                        if (userBo != null) {


                            updateDragItem(mEndPosition[0]);

                        }
                    }


                }
        /* if any absentees merchandiser selected */
                if (mSelecteAbsenteesID != null) {

                    updateList(mSelecteAbsenteesID);
                } else {

                    updateList(0 + "");
                }

                return true;

            default:
                return true;

        }

    }

    public void updateAdapter() {

        PresentDetailAdapter adapter = new PresentDetailAdapter(mPresentIDList);
        mPresentRetailerAllocLV.setAdapter(adapter);
        mTemperorayPresentList = new ArrayList<TaskAssignBO>();


    }

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case 0:
                return new AlertDialog.Builder(this)
                        .setCancelable(false)

                        .setMessage(
                                getResources().getString(
                                        R.string.do_u_want_remove_retailer))
                        .setPositiveButton(getResources().getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {

                                        // Rollback the review plan if review
                                        // done not order or stock
                                        // mAbsenteesList.add(mSelectedDropBO);
                                        mTemperorayPresentList = mPresentTaskListByMerchandiserID
                                                .get(mPresentIDList
                                                        .get(mSelectedGroupPos));

                                        mTemperorayPresentList
                                                .remove(mSelectedChildPos);


                                        mPresentTaskListByMerchandiserID.put(mPresentIDList
                                                .get(mSelectedGroupPos), mTemperorayPresentList);

                                        addItem(mSelectedDropBO);

                                        if (mSelecteAbsenteesID != null) {

                                            updateList(mSelecteAbsenteesID);
                                        } else {
                                            updateList(0 + "");
                                        }

                                        updateAdapter();

                                    }
                                })
                        .setNegativeButton(
                                getResources().getString(R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {

                                    }
                                }).create();
            case 1:
                return new AlertDialog.Builder(this)
                        .setCancelable(false)
                        .setTitle(getResources().getString(R.string.no_connection))

                        .setPositiveButton(getResources().getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {

                                        // Rollback the review plan if review
                                        // done not order or stock
                                        // mAbsenteesList.add(mSelectedDropBO);

                                    }
                                }).create();
            case 2:
                return new AlertDialog.Builder(this)
                        .setCancelable(false)
                        .setTitle(
                                getResources().getString(
                                        R.string.upload_failed_please_try_again))

                        .setPositiveButton(getResources().getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {

                                        // Rollback the review plan if review
                                        // done not order or stock
                                        // mAbsenteesList.add(mSelectedDropBO);

                                    }
                                })
                        .setNegativeButton(
                                getResources().getString(R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {

                                    }
                                }).create();
            case 3:
                return new AlertDialog.Builder(this)
                        .setCancelable(false)
                        .setTitle(
                                getResources().getString(
                                        R.string.data_upload_completed_sucessfully))

                        .setPositiveButton(getResources().getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        Intent i = new Intent(
                                                ReAllocationActivity.this,
                                                HomeScreenActivity.class);
                                        startActivity(i);
                                        finish();

                                    }
                                })

                        .create();
        }
        return null;
    }

    public void startDrag(View v, TaskAssignBO taskBO) {
        v.setBackgroundColor(getResources().getColor(R.color.Yellow));
        TaskAssignBO taskAssignBO = taskBO;
        String retaiername = "";


        retaiername = taskAssignBO.getRetailerName();


        ClipData.Item item = new ClipData.Item(retaiername);
        mViewHolder = (ViewHolder) v.getTag();

        String[] clipDescription = {ClipDescription.MIMETYPE_TEXT_PLAIN};
        ClipData dragData = new ClipData(
                (CharSequence) mViewHolder.RetailerTV.getText(),
                clipDescription, item);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View vg = (View) inflater.inflate(R.layout.list_task_assign, null);
        TextView tv = (TextView) vg.findViewById(R.id.tv_retailer_name);


        tv.setText(retaiername);

        vg.refreshDrawableState();

        mSelectedDropBO = taskAssignBO;

        View.DragShadowBuilder myShadow = new View.DragShadowBuilder(v);

        v.startDrag(dragData, // ClipData
                myShadow, // View.DragShadowBuilder
                retaiername.toString(), // Object myLocalState
                0); // flags

    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // Change color if Filter is selected

        // If the nav drawer is open, hide action items related to the content
        // view

        menu.findItem(R.id.menu_next).setVisible(true);


        return super.onPrepareOptionsMenu(menu);

    }

    // depends on userid,Absentees retailer listview will be generated
    public void updateList(String userid) {
        mSelectedAbsenteesList = new ArrayList<TaskAssignBO>();
        for (TaskAssignBO taskBO : mAbsenteesList) {
            if (taskBO.getUseid().equals(userid)) {
                mSelectedAbsenteesList.add(taskBO);
            }
        }

        if (!userid.equals("0")) {

            retailerAdapter = new AbsenteesRetailerAdapter(
                    mSelectedAbsenteesList);
        } else {
            retailerAdapter = new AbsenteesRetailerAdapter(mAbsenteesList);
        }

        mAbsenteesRetailersLV.setAdapter(retailerAdapter);

    }

    /*
     * while drop any retailer from Absentees list to Present expandable
     * List,selected dropped retailer remove from Absentees list
     */
    public void removeItem(TaskAssignBO selectedTaskBO, String selectuserid) {

        mSelectedAbsenteesList = new ArrayList<TaskAssignBO>();
        if (!selectuserid.equals(0 + "")) {

            int count = -1;
            for (TaskAssignBO taskABo : mAbsenteesList) {
                count = count + 1;

                if (taskABo.getUseid().equals(selectedTaskBO.getUseid())
                        && taskABo.getRetailerID() == selectedTaskBO.getRetailerID()) {

                    break;
                }
            }
            mAbsenteesList.remove(count);

        } else {
            mAbsenteesList.remove(mSelectedDropPos);

        }

    }

    /*
     * Assigned retailer in present merchandiser,while removing it will add in
     * Absentees List
     */
    public void addItem(TaskAssignBO selectedTaskBO) {
        mSelectedAbsenteesList = new ArrayList<TaskAssignBO>();
        mAbsenteesList.add(selectedTaskBO);

    }

    public void updateDragItem(int endPosition) {
        // set IsAlreadyPresent=0 for remove form Present expandable list view
        // if we want

		/* add Dragged Retailer item in list */
        if (mTemperorayPresentList == null)
            mTemperorayPresentList = new ArrayList<>();

        mTemperorayPresentList.add(mSelectedDropBO);
		/* get mSelected Absentees Retailer List depends on userid */
        mSelectedAbsenteesList = mAbsenteesTaskListByMerchandiserID
                .get(mSelectedDropBO.getUseid());
        updateAdapter();

        if (mSelecteAbsenteesID != null) {
            removeItem(mSelectedDropBO, mSelecteAbsenteesID + "");

        } else {

            removeItem(mSelectedDropBO, 0 + "");

        }

    }


    private void updatePresentRetailerHashMap() {
        for (String presentIdList : mPresentIDList) {
            mPresentTaskListByMerchandiserID.put(presentIdList, new ArrayList<TaskAssignBO>());
        }
    }


    public Handler getHandler() {
        return handler;
    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            bmodel = (BusinessModel) getApplicationContext();
            isClicked = false;

            switch (msg.what) {

                case DataMembers.NOTIFY_UPLOADED:
                    //	pd.dismiss();
                    alertDialog.dismiss();
                    bmodel.showAlert(
                            getResources().getString(
                                    R.string.successfully_uploaded),
                            3333);
                    break;
                case DataMembers.NOTIFY_UPLOAD_ERROR:
                    //	pd.dismiss();
                    alertDialog.dismiss();
                    bmodel = (BusinessModel) getApplicationContext();
                    bmodel.showAlert(
                            "Error: "
                                    + getResources().getString(
                                    R.string.upload_failed_please_try_again), 3333);
                    break;
                case DataMembers.NOTIFY_CONNECTION_PROBLEM:
                    alertDialog.dismiss();
                    bmodel = (BusinessModel) getApplicationContext();
                    bmodel.showAlert(
                            "Error: "
                                    + getResources().getString(
                                    R.string.sales_return_saved_locally), 3333);
                    break;


                default:
                    alertDialog.dismiss();
                    bmodel = (BusinessModel) getApplicationContext();
                    bmodel.showAlert(
                            "Error: "
                                    + getResources().getString(
                                    R.string.upload_failed_please_try_again), 3333);
                    break;
            }
        }
    };

    @SuppressLint("NewApi")
    class PresentDetailAdapter extends BaseAdapter {
        private ArrayList<String> presentList;

        public PresentDetailAdapter(ArrayList<String> presentees) {
            // TODO Auto-generated constructor stub
            super();
            this.presentList = presentees;

        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return presentList.size();
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
            final ViewHolder holder;
            if (convertView == null) {
                LayoutInflater inflater = getLayoutInflater();
                convertView = inflater.inflate(R.layout.list_realloc_retailer,
                        parent, false);
                holder = new ViewHolder();
                holder.userTV = (TextView) convertView.findViewById(R.id.tv_present);
                holder.retailerCountBTN = (FloatingActionButton) convertView.findViewById(R.id.fab2);
                holder.retailerCountTV = (TextView) convertView.findViewById(R.id.tv_retailer_count);
                holder.retailerListLL = (LinearLayout) convertView.findViewById(R.id.ll_retailer_name);
                holder.presentNameLL = (LinearLayout) convertView.findViewById(R.id.ll_username);
                holder.retailerCountFL = (FrameLayout) convertView.findViewById(R.id.fl_retailer_count);
                holder.retailerListLL.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (holder.retailerList.size() > 0) {
                            FragmentManager fm = getSupportFragmentManager();
                            RemovePresentRetailerDialog dialogFragment = new RemovePresentRetailerDialog();
                            Bundle bundle = new Bundle();
                            bundle.putString("title", "Retailers");
                            bundle.putString("userid", mPresentIDList.get(holder.pos));
                            ArrayList<TaskAssignBO> retailerList = mPresentTaskListByMerchandiserID.get(mPresentIDList.get(holder.pos));
                            bundle.putSerializable("retailerlist",
                                    retailerList);
                            dialogFragment.setArguments(bundle);


                            dialogFragment.show(fm, "Sample Fragment");
                        }


                    }
                });


                convertView.setOnLongClickListener(new View.OnLongClickListener() {

                    @Override
                    public boolean onLongClick(View v) {
                        return false;

                    }
                });

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.view = convertView;
            holder.presentUserID = presentList.get(position);
            holder.pos = position;
            String presentName = bmodel.teamLeadermasterHelper
                    .getMerchandiserNameByUserID().get(holder.presentUserID);
            holder.userTV.setText(presentName);

            holder.retailerList = mPresentTaskListByMerchandiserID.get(holder.presentUserID);
            final int size = holder.retailerList.size();
            if (size > 0) {

                holder.presentNameLL.getLayoutParams().height = dpToPx(50);
                holder.retailerListLL.getLayoutParams().height = dpToPx(100);
                holder.retailerCountTV.setText(size + "");

                holder.retailerListLL.removeAllViews();
                for (TaskAssignBO taskAssignBO : holder.retailerList) {
                    TextView tv = new TextView(ReAllocationActivity.this);
                    tv.setText(taskAssignBO.getRetailerName());


                    LinearLayoutCompat.LayoutParams params = new LinearLayoutCompat.LayoutParams(
                            LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                            LinearLayoutCompat.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(dpToPx(5), 0, 0, 0);
                    tv.setLayoutParams(params);
                    holder.retailerListLL.addView(tv);
                }

            } else {

                holder.userTV.setHeight(dpToPx(50));
                holder.userTV.setTextSize(dpToPx(12));
                holder.retailerListLL.removeAllViews();
                holder.retailerCountTV.setText(0 + "");


            }
            // TODO Auto-generated method stub
            return convertView;
        }
    }

    /*
    This method is available in AppUtil
     */
    @Deprecated
    private int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    private int pxToDp(int px) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int dp = Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return dp;
    }


}

