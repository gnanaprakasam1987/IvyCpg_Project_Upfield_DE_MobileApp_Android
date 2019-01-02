package com.ivy.ui.task.view;

import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.base.view.BaseActivity;
import com.ivy.cpg.view.task.Task;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ChannelBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.view.HomeScreenActivity;
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


    private int taskChannelId;
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

    @BindView(R.id.rg)
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
                .taskModule(new TaskModule(this))
                .ivyAppComponent(((BusinessModel) getApplication()).getComponent())
                .build()
                .inject(this);

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
    }

    @Override
    protected void setUpViews() {
        setUnBinder(ButterKnife.bind(this));
        setUpToolBar();
        if (!isRetailerTask) {
            setUpUserAdapter();
            setUpChannelAdapter();
            setUpRetailerAdapter();
        }
    }

    @Override
    public void setTaskChannelListData(Vector<ChannelBO> channelList) {

        channelArrayAdapter.clear();
       /* channelArrayAdapter.addAll(channelList);
        ChannelBO channelBO = new ChannelBO();
        channelBO.setChannelId(0);
        channelBO.setChannelName(getString(R.string.all_channel));
        channelArrayAdapter.insert(channelBO, 0);*/

        channelArrayAdapter.add(new ChannelBO(0, getString(R.string.all_channel)));
        for (ChannelBO channelBO : channelList) {
            channelArrayAdapter.add(channelBO);
        }
        channelArrayAdapter.notifyDataSetChanged();
    }

    @Override
    public void setTaskRetailerListData(ArrayList<RetailerMasterBO> retailerList) {
        retailerMasterArrayAdapter.clear();
        retailerMasterArrayAdapter.add(new RetailerMasterBO(0, getString(R.string.all_retailer)));
        for (RetailerMasterBO masterBO : retailerList) {
            retailerMasterArrayAdapter.add(masterBO);
        }
        retailerMasterArrayAdapter.notifyDataSetChanged();
    }

    @Override
    public void setTaskUserListData(ArrayList<UserMasterBO> userList) {
        userMasterArrayAdapter.clear();
        userMasterArrayAdapter.add(new UserMasterBO(0, getString(R.string.select_seller)));
        for (UserMasterBO userMasterBO : userList) {
            userMasterArrayAdapter.add(userMasterBO);
        }
        userMasterArrayAdapter.notifyDataSetChanged();

    }

    @Override
    public String getTaskMode() {
        return mode;
    }

    TypedArray typearr = this.getTheme().obtainStyledAttributes(R.styleable.MyTextView);
    final int color = typearr.getColor(R.styleable.MyTextView_accentcolor, 0);
    final int secondary_color = typearr.getColor(R.styleable.MyTextView_textColorSecondary, 0);

    @OnCheckedChanged(R.id.rg)
    public void onTaskCheckChangedListener(CompoundButton radioBtn, boolean isChecked) {

        switch (radioBtn.getId()) {
            case R.id.seller:
                seller_rb.setTextColor(color);
                channelwise_rb.setTextColor(secondary_color);
                retailerwise_rb.setTextColor(secondary_color);
                channelSpinner.setSelection(0);
                channelSpinner.setEnabled(false);
                retailerSpinner.setSelection(0);
                retailerSpinner.setEnabled(false);
                sellerSpinner.setEnabled(true);
                mode = "seller";
                break;
            case R.id.Channelwise:
                seller_rb.setTextColor(secondary_color);
                channelwise_rb.setTextColor(color);
                retailerwise_rb.setTextColor(secondary_color);
                channelSpinner.setEnabled(true);
                retailerSpinner.setSelection(0);
                retailerSpinner.setEnabled(false);
                sellerSpinner.setSelection(0);
                sellerSpinner.setEnabled(false);
                mode = "channel";
                break;
            case R.id.Retailerwise:
                seller_rb.setTextColor(secondary_color);
                channelwise_rb.setTextColor(secondary_color);
                retailerwise_rb.setTextColor(color);
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
    public void onChannelSpinnerSelected(Spinner spinner, int position) {
        ((TextView) spinner.getSelectedView().findViewById(android.R.id.text1)).setGravity(Gravity.START);
        ChannelBO chBo = (ChannelBO) spinner.getItemAtPosition(position);
        if (chBo.getChannelName().equalsIgnoreCase(getResources().getString(R.string.all_channel))) {
            channelId = -1;
        } else {
            channelId = chBo.getChannelId();
        }

    }

    @OnItemSelected(R.id.retailer)
    public void onRetailerSpinnerSelected(Spinner spinner, int position) {
        ((TextView) spinner.getSelectedView().findViewById(android.R.id.text1)).setGravity(Gravity.START);
        RetailerMasterBO reBo = (RetailerMasterBO) spinner.getItemAtPosition(position);
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
                taskChannelId = 0;
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
    public void updateListData() {

    }


    @OnClick(R.id.saveTask)
    public void onSaveClickBtn() {
        String taskDetailDesc = AppUtils.validateInput(taskView.getText().toString());
        String taskTitleDec = AppUtils.validateInput(taskTitle.getText().toString());

        if (!validate())
            return;

/*        switch (mode) {
            case "seller":
                if (mSelectedUserId == 0)
                    taskChannelId = bmodel.userMasterHelper.getUserMasterBO().getUserid();
                else
                    taskChannelId = userMasterArrayAdapter.getItem(mSelectedUserId).getUserid();

                break;
            case "retailer":
                if (fromHomeScreen)
                    taskChannelId = retailerid;
                else
                    taskChannelId = SDUtil.convertToInt(bmodel.getRetailerMasterBO().getRetailerID());
                break;
            default:
                taskChannelId = channelId;
                break;
        }*/
        taskPresenter.onSaveButtonClick(channelId, taskTitleDec, taskDetailDesc);
    }


    private boolean validate() {
        boolean ok = true;

        if (taskTitle.getText().toString().equals("")) {
            Toast.makeText(this,
                    getResources().getString(R.string.enter_task_title),
                    Toast.LENGTH_SHORT).show();
            ok = false;
        } else if (taskView.getText().toString().equals("")) {
            Toast.makeText(this,
                    getResources().getString(R.string.enter_task_description),
                    Toast.LENGTH_SHORT).show();
            ok = false;
        }
        return ok;
    }

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
