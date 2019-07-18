package com.ivy.cpg.view.task;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.tabs.TabLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.cpg.view.survey.SurveyHelperNew;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.cpg.view.homescreen.HomeScreenActivity;
import com.ivy.sd.png.util.view.OnSingleClickListener;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.sd.png.view.ReasonPhotoDialog;
import com.ivy.ui.task.model.TaskDataBO;
import com.ivy.utils.DateTimeUtils;

import java.util.Vector;

/**
 * Created by ramkumard on 3/4/18
 */

public class TaskFragment extends IvyBaseFragment {

    private BusinessModel bmodel;
    private boolean bool;
    private boolean survey_boolean;
    private boolean hide_new_menu = true;
    private boolean IsRetailerwisetask = false;
    private boolean fromHomeScreen = false;
    private DrawerLayout mDrawerLayout;
    private View view;
    private Vector<TaskDataBO> taskDataBOForAdapter = new Vector<>();
    private LinearLayout mTaskContainer;
    String[] chids;
    private String mSelectedRetailerID = "0";
    private TextView mSelectedTaskTV;
    private Bundle extras;
    private TaskHelper taskHelper;

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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.task_fragment, container, false);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        taskHelper = TaskHelper.getInstance(getActivity());
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        extras = getArguments();
        if (extras == null)
            extras = getActivity().getIntent().getExtras();

        mDrawerLayout = view.findViewById(R.id.drawer_layout);
        // set a custom shadow that overlays the main content when the drawer
        // opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.END);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(getActivity(), /* host Activity */
                mDrawerLayout, /* DrawerLayout object */
                R.string.ok, /* "open drawer" description for accessibility */
                R.string.close /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                //if (((AppCompatActivity) getActivity()).getSupportActionBar() != null)
                setScreenTitle(bmodel.mSelectedActivityName);
                getActivity().invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getActivity().invalidateOptionsMenu();
            }
        };
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        final TabLayout tabLayout = view.findViewById(R.id.tabs);
        mTaskContainer = view.findViewById(
                R.id.task_cintainer_ll);
        if (mTaskContainer != null)
            mTaskContainer.removeAllViews();


        String[] reason = getActivity().getResources().getStringArray(
                R.array.task_tab_header);

        if (bmodel.configurationMasterHelper.IS_SHOW_ONLY_SERVER_TASK) {
            reason = new String[1];
            reason[0] = "All";
        }

        // Add tabs to Tablayout
        for (String tab_name : reason) {
            @SuppressLint("InflateParams")
            TextView tabOne = (TextView) LayoutInflater.from(getActivity()).inflate(R.layout.custom_tab, null);
            tabOne.setText(tab_name);
            tabLayout.addTab(tabLayout.newTab().setCustomView(tabOne));
        }

        if (extras != null) {
            if (extras.containsKey("IsRetailerwisetask")) {
                IsRetailerwisetask = extras.getBoolean("IsRetailerwisetask");
            }

            if (extras.containsKey("fromHomeScreen")) {
                fromHomeScreen = extras.getBoolean("fromHomeScreen");
            }
        }

        if (IsRetailerwisetask) {
            if (bmodel.getRetailerMasterBO() != null)
                if (bmodel.getRetailerMasterBO().getRetailerID().equals("null")) {
                    mSelectedRetailerID = "0";
                } else {
                    mSelectedRetailerID = (bmodel.getRetailerMasterBO()
                            .getRetailerID());
                }
            else
                mSelectedRetailerID = "0";
        }

        // if (bmodel.configurationMasterHelper.IS_SHOW_ONLY_SERVER_TASK) {
        //    addServerTaskFragments();
        //  } else {
        //      addTaskFragments();
        //  }

        if (!bmodel.configurationMasterHelper.IS_NEW_TASK) {
            hideNewTaskMenu();
        }


        if (bmodel.configurationMasterHelper.MOVE_NEXT_ACTIVITY) {
            LinearLayout footer = view.findViewById(R.id.footer);
            footer.setVisibility(View.VISIBLE);

            Button btnClose = view.findViewById(R.id.btn_close);
            btnClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new CommonDialog(getActivity().getApplicationContext(), getActivity(),
                            "", getResources().getString(R.string.move_next_activity),
                            false, getResources().getString(R.string.ok),
                            getResources().getString(R.string.cancel), new CommonDialog.PositiveClickListener() {
                        @Override
                        public void onPositiveButtonClick() {
                            Intent intent = new Intent(getActivity(), HomeScreenTwo.class);

                            if (extras != null) {
                                intent.putExtra("IsMoveNextActivity", true);
                                intent.putExtra("CurrentActivityCode", extras.getString("CurrentActivityCode", ""));
                            }

                            startActivity(intent);
                            getActivity().finish();
                        }
                    }, new CommonDialog.negativeOnClickListener() {
                        @Override
                        public void onNegativeButtonClick() {

                        }
                    }).show();

                }
            });
        }

        if (IsRetailerwisetask) {
            SurveyHelperNew surveyHelperNew = SurveyHelperNew.getInstance(getActivity());
            String chnanelIdForSurvey = surveyHelperNew.getChannelidForSurvey();
            if (chnanelIdForSurvey != null)
                if (chnanelIdForSurvey.length() > 0) {
                    survey_boolean = true;
                    chids = chnanelIdForSurvey.split(",");
                }
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                updateTasks(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        if (bmodel.configurationMasterHelper.IS_SHOW_ONLY_SERVER_TASK) {
            updateTasks(1);
        } else {
            updateTasks(0);
        }

        return view;
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

        //Set Screen Title
        try {
            String title = "";
            if (extras != null) {
                if (extras.containsKey("screentitle")) {
                    title = extras.getString("screentitle");
                } else if (IsRetailerwisetask) {
                    title = bmodel.configurationMasterHelper.getHomescreentwomenutitle("MENU_TASK");
                } else if (fromHomeScreen) {
                    title = bmodel.getMenuName("MENU_TASK_NEW");
                }
            }
            if (title != null && title.equals(""))
                title = getResources().getString(R.string.task);
            setScreenTitle(title);
        } catch (Exception ex) {
            Commons.printException(ex);
            setScreenTitle(getResources().getString(R.string.task));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        bool = false;
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbindDrawables(view.findViewById(R.id.root));
    }

    /**
     * this would clear all the resources used of the layout.
     *
     * @param view unbined view
     */
    private void unbindDrawables(View view) {
        if (view.getBackground() != null) {
            view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            try {
                if (!(view instanceof AdapterView<?>))
                    ((ViewGroup) view).removeAllViews();
            } catch (Exception e) {
                Commons.printException(e);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; getActivity() adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_task, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        // Funcnality Change, While clicking task in checkbox, save will happen
        boolean drawerOpen = false;
        if (mDrawerLayout != null)
            drawerOpen = mDrawerLayout.isDrawerOpen(GravityCompat.END);
        menu.findItem(R.id.menu_save).setVisible(false);
        if (!hide_new_menu)
            menu.findItem(R.id.menu_new_task).setVisible(false);

        if (!fromHomeScreen)//this is applicable for store wise task
            menu.findItem(R.id.menu_reason).setVisible(bmodel.configurationMasterHelper.floating_np_reason_photo);

        if (drawerOpen)
            menu.clear();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i1 = item.getItemId();
        if (i1 == android.R.id.home) {
            if (!bool) {
                // old code if (IsRetailerwisetask && !fromReviewScreen) {
                // Comment by Gp, Issue while going back from Activity Menu
                if (IsRetailerwisetask) {
                    bmodel.outletTimeStampHelper.updateTimeStampModuleWise(DateTimeUtils.now(DateTimeUtils.TIME));
                }
                bool = true;
                if (fromHomeScreen)
                    startActivity(new Intent(getActivity(), HomeScreenActivity.class));
            }
            getActivity().overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            getActivity().finish();
            return true;
        } else if (i1 == R.id.menu_new_task) {
            bmodel.outletTimeStampHelper.updateTimeStampModuleWise(DateTimeUtils
                    .now(DateTimeUtils.TIME));
            Intent i = new Intent(getActivity(), TaskCreation.class);
            i.putExtra("fromHomeScreen", fromHomeScreen);
            i.putExtra("IsRetailerwisetask", IsRetailerwisetask);
            i.putExtra("screentitle", extras.containsKey("screentitle") ? extras.getString("screentitle") : getResources().
                    getString(R.string.task));
            startActivity(i);
            getActivity().finish();
            return true;
        } else if (i1 == R.id.menu_reason) {
            bmodel.reasonHelper.downloadNpReason(bmodel.retailerMasterBO.getRetailerID(), "MENU_TASK");
            ReasonPhotoDialog dialog = new ReasonPhotoDialog();
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (bmodel.reasonHelper.isNpReasonPhotoAvaiable(bmodel.retailerMasterBO.getRetailerID(), "MENU_TASK")) {
                        if (!fromHomeScreen) {
                            bmodel.saveModuleCompletion("MENU_TASK", true);
                            startActivity(new Intent(getActivity(),
                                    HomeScreenTwo.class));
                            getActivity().finish();

                        }
                    }
                }
            });
            Bundle args = new Bundle();
            args.putString("modulename", "MENU_TASK");
            dialog.setCancelable(false);
            dialog.setArguments(args);
            dialog.show(getActivity().getSupportFragmentManager(), "ReasonDialogFragment");
            return true;
        }
        return false;
    }

    public void hideNewTaskMenu() {
        hide_new_menu = false;
    }


    @SuppressLint("SetTextI18n")
    private void updateTasks(int taskType) {
        Vector<TaskDataBO> taskDataBO = taskHelper.getTaskData(mSelectedRetailerID);
        taskDataBOForAdapter.clear();
        try {
            if (taskDataBO != null) {
                int size = taskDataBO.size();

                TaskDataBO taskData;
                for (int i = 0; i < size; i++) {
                    taskData = taskDataBO.elementAt(i);

                    if (IsRetailerwisetask) {
                        if (mSelectedRetailerID.equals(taskData.getRid() + "")) {
                            if (taskType == 1) { // server
                                if (taskData.getUsercreated().toUpperCase()
                                        .equals("0")) {
                                    taskDataBOForAdapter.add(taskData);
                                }
                            } else if (taskType == 2) { // user
                                if (taskData.getUsercreated().toUpperCase()
                                        .equals("1")) {
                                    taskDataBOForAdapter.add(taskData);
                                }
                            } else {
                                taskDataBOForAdapter.add(taskData);
                            }
                        } else if (survey_boolean) {
                            int chid;
                            for (String chid1 : chids) {
                                chid = SDUtil.convertToInt(chid1);
                                if (taskData.getChannelId() == chid) {
                                    if (taskType == 1) { // server
                                        if (taskData.getUsercreated().toUpperCase()
                                                .equals("0")) {
                                            taskDataBOForAdapter.add(taskData);
                                        }
                                    } else if (taskType == 2) { // user
                                        if (taskData.getUsercreated().toUpperCase()
                                                .equals("1")) {
                                            taskDataBOForAdapter.add(taskData);
                                        }
                                    } else {
                                        taskDataBOForAdapter.add(taskData);
                                    }
                                }
                            }

                        }
                    } else {

                        if (taskData.getRid() == 0
                                && taskData.getChannelId() == 0
                                && (taskData.getUserId() == bmodel.userMasterHelper.getUserMasterBO().getUserid()
                                || taskData.getUserId() == 0)) {

                            if (taskType == 1) { // server
                                if (taskData.getUsercreated().toUpperCase()
                                        .equals("0")) {
                                    taskDataBOForAdapter.add(taskData);
                                }
                            } else if (taskType == 2) { // user
                                if (taskData.getUsercreated().toUpperCase()
                                        .equals("1")) {
                                    taskDataBOForAdapter.add(taskData);
                                }
                            } else {
                                taskDataBOForAdapter.add(taskData);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
        mTaskContainer.removeAllViews();

        LayoutInflater inflater = getActivity().getLayoutInflater();

        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.row_task_heading, null);
        TextView task_execution = view.findViewById(R.id.task_execution);

        task_execution.setVisibility(View.VISIBLE);
        //layoutTaskHeader.setVisibility(View.VISIBLE);

        mTaskContainer.addView(view);

        if (taskDataBOForAdapter != null) {

            for (int i = 0; i < taskDataBOForAdapter.size(); i++) {
                final ViewHolder holder = new ViewHolder();
                holder.taskBO = taskDataBOForAdapter.get(i);
                TaskDataBO task = holder.taskBO;

                @SuppressLint("InflateParams") View v = inflater.inflate(R.layout.row_task_title, null);

                holder.taskCB = v.findViewById(R.id.task_title_CB);
                //holder.taskTaskOwner = v.findViewById(R.id.task_taskowner);
                //holder.taskCreatedDate = v.findViewById(R.id.task_createdOn);
                holder.layoutCB = v.findViewById(R.id.layoutCB);
                holder.layoutrow = v.findViewById(R.id.layoutBorder);

                holder.layoutCB.setVisibility(View.VISIBLE);
                //holder.taskTaskOwner.setText(" : " + task.getTaskOwner());
                //holder.taskCreatedDate.setText(DateTimeUtils.convertFromServerDateToRequestedFormat(task.getCreatedDate(), ConfigurationMasterHelper.outDateFormat) + ", ");

                holder.layoutrow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!holder.taskCB.isChecked()) {
                            holder.taskCB.setChecked(true);
                            holder.taskBO.setChecked(true);
                        } else {
                            holder.taskCB.setChecked(false);
                            holder.taskBO.setChecked(false);
                        }

                        bmodel.outletTimeStampHelper.updateTimeStampModuleWise(DateTimeUtils.now(DateTimeUtils.TIME));
                        if (IsRetailerwisetask) {
                            taskHelper.saveTask(bmodel
                                    .getRetailerMasterBO()
                                    .getRetailerID(), holder.taskBO);
                        } else {
                            taskHelper.saveTask(0 + "",
                                    holder.taskBO);
                        }
                    }
                });
                holder.layoutCB.setOnClickListener(new OnSingleClickListener() {
                    @Override
                    public void onSingleClick(View v) {
                        holder.taskCB.setChecked(!holder.taskCB.isChecked());
                    }
                });
                holder.taskCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                        holder.taskCB.setChecked(isChecked);
                        holder.taskBO.setChecked(isChecked);

                        bmodel.outletTimeStampHelper.updateTimeStampModuleWise(DateTimeUtils.now(DateTimeUtils.TIME));
                        if (IsRetailerwisetask) {
                            taskHelper.saveTask(bmodel
                                    .getRetailerMasterBO()
                                    .getRetailerID(), holder.taskBO);
                        } else {
                            taskHelper.saveTask(0 + "",
                                    holder.taskBO);
                        }
                    }
                });
                holder.taskTitle = v
                        .findViewById(R.id.task_title_tv);
                holder.taskTitle.setText(task.getTasktitle());
                holder.taskTitle.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        mSelectedTaskTV = holder.taskDescription;
                        mSelectedTaskTV.setVisibility(View.VISIBLE);
                    }
                });

                holder.taskDescription = v
                        .findViewById(R.id.task_category_tv);
                holder.taskDescription.setText(task.getTaskDesc());
                if (task.isUpload() && task.getIsdone().equals("1")) {
                    holder.taskCB.setEnabled(false);
                    holder.taskTitle.setPaintFlags(holder.taskTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    holder.taskDescription.setPaintFlags(holder.taskDescription.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                } else {
                    holder.taskCB.setEnabled(true);
                    holder.taskTitle.setPaintFlags(holder.taskTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                    holder.taskDescription.setPaintFlags(holder.taskDescription.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                }

                if (task.getIsdone().equals("1") && !task.isUpload()) {
                    holder.taskCB.setChecked(true);
                    holder.taskBO.setChecked(true);
                } else {
                    holder.taskCB.setChecked(false);
                    holder.taskBO.setChecked(false);
                }

                mTaskContainer.addView(v);

            }
            mTaskContainer.invalidate();
        }
    }

    class ViewHolder {
        TaskDataBO taskBO;
        CheckBox taskCB;
        TextView taskTitle;
        TextView taskDescription;
        //TextView taskTaskOwner;
        TextView taskCreatedDate;
        RelativeLayout layoutCB;
        RelativeLayout layoutrow;
    }

}
