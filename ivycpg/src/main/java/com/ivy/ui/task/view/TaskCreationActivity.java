package com.ivy.ui.task.view;

import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.base.view.BaseActivity;
import com.ivy.cpg.view.homescreen.HomeScreenActivity;
import com.ivy.cpg.view.task.Task;
import com.ivy.cpg.view.task.TaskDataBO;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ChannelBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.ui.task.TaskContract;
import com.ivy.ui.task.di.DaggerTaskComponent;
import com.ivy.ui.task.di.TaskModule;
import com.ivy.utils.AppUtils;

import java.util.ArrayList;
import java.util.Vector;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnItemSelected;

public class TaskCreationActivity extends BaseActivity implements TaskContract.TaskView {

    @Inject
    TaskContract.TaskPresenter<TaskContract.TaskView> taskPresenter;

    private int channelId, retailerid;
    private boolean fromHomeScreen = false;
    private boolean isRetailerTask = false;
    private String screenTitle = "";
    private String mode = "seller";
    private int mSelectedUserId = 0;

    @BindView(R.id.taskView)
    EditText taskView;

    @BindView(R.id.tv)
    EditText taskTitle;

    @BindView(R.id.spinner_seller)
    Spinner sellerSpinner;

    @BindView(R.id.channel)
    Spinner channelSpinner;

    @BindView(R.id.retailer)
    Spinner retailerSpinner;

    @BindView(R.id.rg_selection)
    RadioGroup radioGroup;

    @BindView(R.id.seller)
    RadioButton seller_rb;

    @BindView(R.id.Channelwise)
    RadioButton channelwise_rb;

    @BindView(R.id.Retailerwise)
    RadioButton retailerwise_rb;

    @BindView(R.id.saveTask)
    Button saveBtn;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.applicable_tv)
    TextView applicableTV;

    @BindView(R.id.task_spinner_layouts)
    LinearLayout spinnerLayout;

    private ArrayAdapter<UserMasterBO> userMasterArrayAdapter;

    private ArrayAdapter<ChannelBO> channelArrayAdapter;

    private ArrayAdapter<RetailerMasterBO> retailerMasterArrayAdapter;


    @Override
    public int getLayoutId() {
        return R.layout.activity_task_creation;
    }

    @Override
    protected void initVariables() {

    }

    @Override
    public void initializeDi() {
        DaggerTaskComponent.builder()
                .ivyAppComponent(((BusinessModel) getApplication()).getComponent())
                .taskModule(new TaskModule(this, TaskCreationActivity.this))
                .build()
                .inject(TaskCreationActivity.this);

        setBasePresenter((BasePresenter) taskPresenter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void getMessageFromAliens() {
        if (getIntent().getExtras() != null) {
            fromHomeScreen = getIntent().getExtras().getBoolean("fromHomeScreen", false);
            isRetailerTask = getIntent().getExtras().getBoolean("IsRetailerwisetask", false);
            screenTitle = getIntent().getExtras().getString("screentitle", getString(R.string.task_creation));
        }
        taskPresenter.fetchData();
    }

    @Override
    protected void setUpViews() {
        setUnBinder(ButterKnife.bind(this));
        setUpToolBar();
        //allow only create task only for retailer if not from seller Task
        if (isRetailerTask) {

            radioGroup.setVisibility(View.GONE);
            spinnerLayout.setVisibility(View.GONE);
            applicableTV.setVisibility(View.GONE);
            mode = "retailer";
        } else {
            setUpUserAdapter();
            setUpChannelAdapter();
            setUpRetailerAdapter();
        }
    }

    @Override
    public void setTaskChannelListData(Vector<ChannelBO> channelList) {
        channelArrayAdapter.clear();
        channelArrayAdapter.add(new ChannelBO(0, getString(R.string.all_channel)));
        channelArrayAdapter.addAll(channelList);
        channelArrayAdapter.notifyDataSetChanged();
    }

    @Override
    public void setTaskRetailerListData(ArrayList<RetailerMasterBO> retailerList) {
        retailerMasterArrayAdapter.clear();
        retailerMasterArrayAdapter.add(new RetailerMasterBO(0, getString(R.string.all_retailer)));
        retailerMasterArrayAdapter.addAll(retailerList);
        retailerMasterArrayAdapter.notifyDataSetChanged();
    }

    @Override
    public void setTaskUserListData(ArrayList<UserMasterBO> userList) {
        userMasterArrayAdapter.clear();
        userMasterArrayAdapter.add(new UserMasterBO(0, getString(R.string.select_seller)));
        userMasterArrayAdapter.addAll(userList);
        userMasterArrayAdapter.notifyDataSetChanged();

    }

    @Override
    public String getTaskMode() {
        return mode;
    }

    /*TypedArray typearr = this.getTheme().obtainStyledAttributes(R.styleable.MyTextView);
    final int color = typearr.getColor(R.styleable.MyTextView_accentcolor, 0);
    final int secondary_color = typearr.getColor(R.styleable.MyTextView_textColorSecondary, 0);*/

    @OnClick({R.id.seller, R.id.Channelwise, R.id.Retailerwise})
    public void onTaskCheckChangedListener(RadioButton radioBtn) {

        switch (radioBtn.getId()) {
            case R.id.seller:
                seller_rb.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
                channelwise_rb.setTextColor(ContextCompat.getColor(this, R.color.textColorPrimary));
                retailerwise_rb.setTextColor(ContextCompat.getColor(this, R.color.textColorPrimary));
                channelSpinner.setSelection(0);
                channelSpinner.setEnabled(false);
                retailerSpinner.setSelection(0);
                retailerSpinner.setEnabled(false);
                sellerSpinner.setEnabled(true);
                mode = "seller";
                break;
            case R.id.Channelwise:
                seller_rb.setTextColor(ContextCompat.getColor(this, R.color.textColorPrimary));
                channelwise_rb.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
                retailerwise_rb.setTextColor(ContextCompat.getColor(this, R.color.textColorPrimary));
                channelSpinner.setEnabled(true);
                retailerSpinner.setSelection(0);
                retailerSpinner.setEnabled(false);
                sellerSpinner.setSelection(0);
                sellerSpinner.setEnabled(false);
                mode = "channel";
                break;
            case R.id.Retailerwise:
                seller_rb.setTextColor(ContextCompat.getColor(this, R.color.textColorPrimary));
                channelwise_rb.setTextColor(ContextCompat.getColor(this, R.color.textColorPrimary));
                retailerwise_rb.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
                channelSpinner.setSelection(0);
                channelSpinner.setEnabled(false);
                retailerSpinner.setEnabled(true);
                sellerSpinner.setSelection(0);
                sellerSpinner.setEnabled(false);
                mode = "retailer";
                break;
        }
    }

    @OnItemSelected(R.id.spinner_seller)
    public void onUserSpinnerSelected(Spinner spinner, int position) {
        ((TextView) spinner.getSelectedView().findViewById(android.R.id.text1)).setGravity(Gravity.START);
        mSelectedUserId = position;
    }

    @OnItemSelected(R.id.channel)
    public void onChannelSpinnerSelected(Spinner spinner,int position) {
        ((TextView) spinner.getSelectedView().findViewById(android.R.id.text1)).setGravity(Gravity.START);
        ChannelBO chBo = (ChannelBO) spinner.getSelectedItem();
        if (chBo.getChannelName().equalsIgnoreCase(getResources().getString(R.string.all_channel))) {
            channelId = -1;
        } else {
            channelId = chBo.getChannelId();
        }

    }

    @OnItemSelected(R.id.retailer)
    public void onRetailerSpinnerSelected(Spinner spinner, int position) {
        ((TextView) spinner.getSelectedView().findViewById(android.R.id.text1)).setGravity(Gravity.START);
        RetailerMasterBO reBo = (RetailerMasterBO) spinner.getSelectedItem();
        if (reBo.getTretailerName().equalsIgnoreCase(getResources().getString(R.string.all_retailer))) {
            retailerid = reBo.getTretailerId();
        }
    }

    @Override
    public void showUpdatedDialog() {
        showAlert("", getResources().getString(R.string.saved_successfully), new CommonDialog.PositiveClickListener() {
            @Override
            public void onPositiveButtonClick() {
                taskView.setText("");
                taskTitle.setText("");
                mode = "seller";
                if (fromHomeScreen)
                    startActivity(new Intent(TaskCreationActivity.this,
                            HomeScreenActivity.class).putExtra("menuCode", "MENU_TASK_NEW"));
                else
                    startActivity(new Intent(TaskCreationActivity.this,
                            Task.class).putExtra("IsRetailerwisetask", isRetailerTask)
                            .putExtra("screentitle", screenTitle));
                finish();
            }
        });

    }

    @Override
    public void updateListData(ArrayList<TaskDataBO> updatedList) {

    }


    @OnClick(R.id.saveTask)
    public void onSaveClickBtn() {
        String taskDetailDesc = AppUtils.validateInput(taskView.getText().toString());
        String taskTitleDec = AppUtils.validateInput(taskTitle.getText().toString());
        int taskChannelId;

        if (!taskPresenter.isValidate(taskTitle.getText().toString(), taskView.getText().toString()))
            return;

        switch (mode) {
            case "seller":
                if (mSelectedUserId == 0)
                    taskChannelId = taskPresenter.getUserID();
                else
                    taskChannelId = userMasterArrayAdapter.getItem(mSelectedUserId).getUserid();

                break;
            case "retailer":
                if (fromHomeScreen)
                    taskChannelId = retailerid;
                else
                    taskChannelId = taskPresenter.getRetailerID();
                break;
            default:
                taskChannelId = channelId;
                break;
        }
        taskPresenter.onSaveButtonClick(taskChannelId, taskTitleDec, taskDetailDesc);
    }


    /*private boolean validate() {

        if (taskTitle.getText().toString().equals("")) {
            Toast.makeText(this,
                    getResources().getString(R.string.enter_task_title),
                    Toast.LENGTH_SHORT).show();
            return false;
        } else if (taskView.getText().toString().equals("")) {
            Toast.makeText(this,
                    getResources().getString(R.string.enter_task_description),
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }*/

    private void setUpToolBar() {
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Used to remove the appLogo action bar icon and set title as home
        // (title support click)
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        // Used to hide the appLogo icon from action bar

        getSupportActionBar().setIcon(null);

        getSupportActionBar().setTitle(null);

        setScreenTitle(screenTitle);
    }

    private void setUpUserAdapter() {
        userMasterArrayAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_bluetext_layout);
        userMasterArrayAdapter.setDropDownViewResource(R.layout.spinner_bluetext_list_item);
        sellerSpinner.setAdapter(userMasterArrayAdapter);
    }

    private void setUpChannelAdapter() {
        channelArrayAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_bluetext_layout);
        channelArrayAdapter.setDropDownViewResource(R.layout.spinner_bluetext_list_item);
        channelSpinner.setAdapter(channelArrayAdapter);
    }

    private void setUpRetailerAdapter() {
        retailerMasterArrayAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_bluetext_layout);
        retailerMasterArrayAdapter.setDropDownViewResource(R.layout.spinner_bluetext_list_item);
        retailerSpinner.setAdapter(retailerMasterArrayAdapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_task, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_save).setVisible(false);
        menu.findItem(R.id.menu_new_task).setVisible(false);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            if (fromHomeScreen)
                startActivity(new Intent(TaskCreationActivity.this,
                        HomeScreenActivity.class).putExtra("menuCode", "MENU_TASK_NEW"));
            else
                startActivity(new Intent(TaskCreationActivity.this,
                        Task.class).putExtra("IsRetailerwisetask", isRetailerTask)
                        .putExtra("screentitle", screenTitle));
            finish();
            return true;
        }
        return false;
    }
}
