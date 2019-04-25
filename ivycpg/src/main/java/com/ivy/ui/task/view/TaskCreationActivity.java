package com.ivy.ui.task.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.base.view.BaseActivity;
import com.ivy.cpg.view.homescreen.HomeScreenActivity;
import com.ivy.cpg.view.task.TaskDataBO;
import com.ivy.sd.camera.CameraActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ChannelBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.view.DataPickerDialogFragment;
import com.ivy.ui.task.TaskConstant;
import com.ivy.ui.task.TaskContract;
import com.ivy.ui.task.adapter.TaskImgListAdapter;
import com.ivy.ui.task.di.DaggerTaskComponent;
import com.ivy.ui.task.di.TaskModule;
import com.ivy.utils.AppUtils;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FileUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;

import static com.ivy.core.IvyConstants.DEFAULT_DATE_FORMAT;

public class TaskCreationActivity extends BaseActivity implements TaskContract.TaskCreationView, DataPickerDialogFragment.UpdateDateInterface {

    @Inject
    TaskContract.TaskPresenter<TaskContract.TaskView> taskPresenter;

    private int mSelectedCategoryID = 0;
    private boolean isRetailerWiseTask = false;
    private String menuCode;
    private int screenMode = 0;// 0 - Creation 1 - Edit
    private TaskDataBO taskBo;
    private String screenTitle = "";
    private String mode = TaskConstant.SELLER_WISE;
    private int mSelectedSpinnerPos = 0;
    private String imageName = "";
    private static String folderPath;
    private static final int CAMERA_REQUEST_CODE = 1;
    private static final String TAG_DATE_PICKER_TO = "date_picker_to";

    @BindView(R.id.taskView)
    EditText taskView;

    @BindView(R.id.tv)
    EditText taskTitle;

    @BindView(R.id.spinner_seller)
    Spinner spinnerSelection;

    @BindView(R.id.task_category_spinner)
    AppCompatSpinner categorySpinner;

    @BindView(R.id.task_category_tv)
    TextView productLevelTV;

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

    @BindView(R.id.task_img_recycler_view)
    RecyclerView imgListRecyclerView;

    @BindView(R.id.task_due_date_btn)
    Button dueDateBtn;

    private ArrayAdapter<UserMasterBO> userMasterArrayAdapter;

    private ArrayAdapter<ChannelBO> channelArrayAdapter;

    private ArrayAdapter<RetailerMasterBO> retailerMasterArrayAdapter;

    private ArrayAdapter<TaskDataBO> taskCategoryArrayAdapter;

    private ArrayList<String> capturedImgList = new ArrayList<>();


    @Override
    public int getLayoutId() {
        return R.layout.activity_task_creation;
    }

    @Override
    protected void initVariables() {

        folderPath = getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/"
                + DataMembers.photoFolderName;
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
            isRetailerWiseTask = getIntent().getExtras().getBoolean(TaskConstant.RETAILER_WISE_TASK, false);
            screenTitle = getIntent().getExtras().getString(TaskConstant.SCREEN_TITLE, getString(R.string.task_creation));
            screenMode = getIntent().getExtras().getInt(TaskConstant.TASK_SCREEN_MODE, 0);
            taskBo = getIntent().getExtras().getParcelable(TaskConstant.TASK_OBJECT);
            menuCode = getIntent().getExtras().getString(TaskConstant.MENU_CODE, "MENU_TASK");
        }
    }

    @Override
    protected void setUpViews() {
        setUnBinder(ButterKnife.bind(this));
        setUpToolBar();
        setUpRecyclerView();
        TaskConstant.TASK_SERVER_IMG_PATH = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/"
                + taskPresenter.getUserID()
                + DataMembers.DIGITAL_CONTENT + "/"
                + DataMembers.TASK_DIGITAL_CONTENT;

        //allow only create task only for retailer if not from seller Task
        if (isRetailerWiseTask) {
            handleViewVisibility(View.GONE);
        } else {
            setUpAdapter();
            taskPresenter.fetchData();
            setUpSpinnerData(0);
        }

        setUpCategoryAdapter();
        taskPresenter.fetchTaskCategory("MENU_TASK");


        if (screenMode == 1)
            taskPresenter.fetchTaskImageList(taskBo.getTaskId());
        else
            taskPresenter.addNewImage("");
    }


    private void handleViewVisibility(int visibilityState) {
        radioGroup.setVisibility(visibilityState);
        spinnerSelection.setVisibility(visibilityState);
        applicableTV.setVisibility(visibilityState);
        mode = "retailer";
    }

    @Override
    public void setTaskChannelListData(ArrayList<ChannelBO> channelList) {
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
        setUpSpinnerData(0);
    }

    @Override
    public void setTaskCategoryListData(ArrayList<TaskDataBO> categoryList) {
        taskCategoryArrayAdapter.clear();
        taskCategoryArrayAdapter.add(new TaskDataBO(0, getString(R.string.plain_select), 1));
        taskCategoryArrayAdapter.addAll(categoryList);
        taskCategoryArrayAdapter.notifyDataSetChanged();
        categorySpinner.setAdapter(taskCategoryArrayAdapter);
    }

    @Override
    public void showTaskTitleError() {
        showMessage(R.string.enter_task_title);
    }

    @Override
    public void showTaskDescError() {
        showMessage(R.string.enter_task_description);
    }

    @Override
    public void showTaskSaveAlertMsg() {
        showAlert("", getString(R.string.saved_successfully), () -> {
            if (!isRetailerWiseTask)
                startActivity(new Intent(TaskCreationActivity.this,
                        HomeScreenActivity.class).putExtra(TaskConstant.MENU_CODE, menuCode));
            else
                startActivity(new Intent(TaskCreationActivity.this,
                        TaskActivity.class).putExtra(TaskConstant.RETAILER_WISE_TASK, isRetailerWiseTask)
                        .putExtra(TaskConstant.SCREEN_TITLE, screenTitle));
            finish();
        });
    }

    @Override
    public void updateImageListAdapter(ArrayList<TaskDataBO> imageList) {
        imgListRecyclerView.setAdapter(new TaskImgListAdapter(TaskCreationActivity.this, imageList, false, photoClickListener));
    }

    @Override
    public void showImageUpdateMsg() {
        showMessage(R.string.image_saved);
    }

    TaskImgListAdapter.PhotoClickListener photoClickListener = this::prepareTaskPhotoCapture;

    @Override
    public String getTaskMode() {
        return mode;
    }

    @OnClick({R.id.seller, R.id.Channelwise, R.id.Retailerwise})
    public void onTaskCheckChangedListener(RadioButton radioBtn) {

        switch (radioBtn.getId()) {
            case R.id.seller:
                seller_rb.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
                channelwise_rb.setTextColor(ContextCompat.getColor(this, R.color.textColorPrimary));
                retailerwise_rb.setTextColor(ContextCompat.getColor(this, R.color.textColorPrimary));
                setUpSpinnerData(0);
                mode = "seller";
                break;
            case R.id.Channelwise:
                seller_rb.setTextColor(ContextCompat.getColor(this, R.color.textColorPrimary));
                channelwise_rb.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
                retailerwise_rb.setTextColor(ContextCompat.getColor(this, R.color.textColorPrimary));
                setUpSpinnerData(1);
                mode = "channel";
                break;
            case R.id.Retailerwise:
                seller_rb.setTextColor(ContextCompat.getColor(this, R.color.textColorPrimary));
                channelwise_rb.setTextColor(ContextCompat.getColor(this, R.color.textColorPrimary));
                retailerwise_rb.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
                setUpSpinnerData(2);
                mode = "retailer";
                break;
        }
    }

    @OnItemSelected(R.id.spinner_seller)
    public void onUserSpinnerSelected(Spinner spinner, int position) {
        ((TextView) spinner.getSelectedView().findViewById(android.R.id.text1)).setGravity(Gravity.START);
        mSelectedSpinnerPos = position;
    }

    @OnItemSelected(R.id.task_category_spinner)
    public void onCategorySpinnerSelected(AppCompatSpinner categorySpinner, int position) {
        ((TextView) categorySpinner.getSelectedView().findViewById(android.R.id.text1)).setGravity(Gravity.START);
        TaskDataBO taskBo = (TaskDataBO) categorySpinner.getSelectedItem();
        if (taskBo.getTaskCategoryID() != 0) {
            mSelectedCategoryID = taskBo.getTaskCategoryID();
        }
    }

    @OnClick(R.id.task_due_date_btn)
    public void onDueDateClick() {
        DataPickerDialogFragment newFragment = new DataPickerDialogFragment();
        newFragment.show(getSupportFragmentManager(), TAG_DATE_PICKER_TO);
    }

    @Override
    public void updateListData(ArrayList<TaskDataBO> updatedList) {
        updatedList.add(0, new TaskDataBO());
        taskPresenter.getTaskImgList().addAll(updatedList);
        imgListRecyclerView.setAdapter(new TaskImgListAdapter(TaskCreationActivity.this, updatedList, false, photoClickListener));
        updateFieldsInEditMode(taskBo);
    }


    @OnClick(R.id.saveTask)
    public void onSaveClickBtn() {
        String taskDetailDesc = AppUtils.validateInput(taskView.getText().toString());
        String taskTitleDec = AppUtils.validateInput(taskTitle.getText().toString());
        String taskDuedate = dueDateBtn.getText().toString().isEmpty() ? null
                : dueDateBtn.getText().toString();
        int taskChannelId;
        if (!taskPresenter.validate(taskTitle.getText().toString(), taskView.getText().toString()))
            return;

        switch (mode) {
            case TaskConstant.SELLER_WISE:
                if (mSelectedSpinnerPos == 0)
                    taskChannelId = taskPresenter.getUserID();
                else
                    taskChannelId = userMasterArrayAdapter.getItem(mSelectedSpinnerPos).getUserid();

                break;
            case TaskConstant.RETAILER_WISE:
                if (!isRetailerWiseTask)
                    taskChannelId = retailerMasterArrayAdapter.getItem(mSelectedSpinnerPos).getTretailerId();
                else
                    taskChannelId = taskPresenter.getRetailerID();
                break;
            default:
                if (mSelectedSpinnerPos == 0)
                    taskChannelId = -1;
                else
                    taskChannelId = channelArrayAdapter.getItem(mSelectedSpinnerPos).getChannelId();
                break;
        }

        if (screenMode == 0)
            taskBo = new TaskDataBO();

        taskBo.setTasktitle(taskTitleDec);
        taskBo.setTaskDesc(taskDetailDesc);
        taskBo.setTaskDueDate(taskDuedate);
        taskBo.setTaskCategoryID(mSelectedCategoryID);

        taskPresenter.onSaveButtonClick(taskChannelId, taskBo);
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

    private void setUpRecyclerView() {
        imgListRecyclerView.setHasFixedSize(true);
        imgListRecyclerView.setNestedScrollingEnabled(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        imgListRecyclerView.setLayoutManager(layoutManager);
    }

    private void setUpAdapter() {
        userMasterArrayAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_blacktext_layout);
        userMasterArrayAdapter.setDropDownViewResource(R.layout.spinner_blacktext_list_item);

        channelArrayAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_blacktext_layout);
        channelArrayAdapter.setDropDownViewResource(R.layout.spinner_blacktext_list_item);

        retailerMasterArrayAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_blacktext_layout);
        retailerMasterArrayAdapter.setDropDownViewResource(R.layout.spinner_blacktext_list_item);
    }

    private void setUpSpinnerData(int isFrom) {
        mSelectedSpinnerPos = 0;

        switch (isFrom) {
            case 0:
                if (userMasterArrayAdapter.getCount() == 2)
                    spinnerSelection.setVisibility(View.GONE);
                spinnerSelection.setAdapter(userMasterArrayAdapter);
                handleVisibility(View.INVISIBLE);
                break;
            case 1:
                spinnerSelection.setVisibility(View.VISIBLE);
                spinnerSelection.setAdapter(channelArrayAdapter);
                if (taskPresenter.isShowProdLevel())
                    handleVisibility(View.VISIBLE);
                break;
            case 2:
                spinnerSelection.setVisibility(View.VISIBLE);
                spinnerSelection.setAdapter(retailerMasterArrayAdapter);
                if (taskPresenter.isShowProdLevel())
                    handleVisibility(View.VISIBLE);
                break;
        }


    }


    private void handleVisibility(int visibility) {
        productLevelTV.setVisibility(visibility);
        categorySpinner.setVisibility(visibility);
    }

    private void setUpCategoryAdapter() {
        taskCategoryArrayAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_blacktext_layout);
        taskCategoryArrayAdapter.setDropDownViewResource(R.layout.spinner_blacktext_list_item);
    }

    private void prepareTaskPhotoCapture() {
        int id = 0;

        if (seller_rb.isChecked()) {
            if (mSelectedSpinnerPos == 0)
                id = taskPresenter.getUserID();
            else
                id = userMasterArrayAdapter.getItem(mSelectedSpinnerPos).getUserid();
        } else if (channelwise_rb.isChecked())
            id = channelArrayAdapter.getItem(mSelectedSpinnerPos).getChannelId();
        else if (retailerwise_rb.isChecked())
            id = retailerMasterArrayAdapter.getItem(mSelectedSpinnerPos).getTretailerId();

        imageName = "TSK_" + id
                + "_" + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID_MILLIS)
                + ".jpg";

        navigateToCameraActivity();

    }

    private void navigateToCameraActivity() {
        Intent intent = new Intent(
                TaskCreationActivity.this,
                CameraActivity.class);
        String _path = FileUtils.photoFolderPath + "/" + imageName;
        intent.putExtra(TaskConstant.FILE_PATH, _path);
        startActivityForResult(intent,
                CAMERA_REQUEST_CODE);
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
            if (!capturedImgList.isEmpty()
                    && capturedImgList.size() > 1)
                backButtonAlertDialog();
            else
                backNavigation();


            return true;
        }
        return false;
    }


    private void backNavigation() {
        if (!isRetailerWiseTask)
            startActivity(new Intent(TaskCreationActivity.this,
                    HomeScreenActivity.class).putExtra(TaskConstant.MENU_CODE, menuCode));
        else
            startActivity(new Intent(TaskCreationActivity.this,
                    TaskActivity.class).putExtra(TaskConstant.RETAILER_WISE_TASK, isRetailerWiseTask)
                    .putExtra(TaskConstant.SCREEN_TITLE, screenTitle));

        finish();
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == 1) {
                capturedImgList.add(imageName);
                taskPresenter.addNewImage(imageName);
            }

        }
    }

    @Override
    public void updateDate(Date date, String tag) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        Calendar selectedDate = new GregorianCalendar(year, month, day);

        if (tag.equals(TAG_DATE_PICKER_TO)) {
            if (selectedDate.getTimeInMillis() >= Calendar.getInstance().getTimeInMillis())
                dueDateBtn.setText(DateTimeUtils.convertDateObjectToRequestedFormat(
                        selectedDate.getTime(), DEFAULT_DATE_FORMAT));
            else
                showMessage(getString(R.string.select_future_date));
        }
    }

    /**
     * Alert dialog while moving back
     */
    private void backButtonAlertDialog() {

        showAlert("", getString(R.string.photo_capture_not_saved_go_back), () -> {

            if (capturedImgList.size() > 0) {
                for (String imageName : capturedImgList)
                    FileUtils.deleteFiles(folderPath, imageName);
            }

            backNavigation();
        }, () -> {

        });
    }

    private void updateFieldsInEditMode(@NotNull TaskDataBO taskDataObj) {
        taskTitle.setText(taskDataObj.getTasktitle());
        if (isRetailerWiseTask)
            categorySpinner.setSelection(getAdapterPosition(taskCategoryArrayAdapter.getCount(), TaskConstant.PRODUCT_LEVEL_WISE));

        dueDateBtn.setText(DateTimeUtils.convertFromServerDateToRequestedFormat(
                taskDataObj.getTaskDueDate(), taskPresenter.outDateFormat()));

        if (!isRetailerWiseTask) {
            if (taskDataObj.getMode().equalsIgnoreCase(TaskConstant.SELLER_WISE)) {
                seller_rb.setChecked(true);
                setUpSpinnerData(0);
                spinnerSelection.setSelection(getAdapterPosition(userMasterArrayAdapter.getCount(), taskBo.getMode()));
            } else if (taskDataObj.getMode().equalsIgnoreCase(TaskConstant.RETAILER_WISE)) {
                retailerwise_rb.setChecked(true);
                setUpSpinnerData(1);
                spinnerSelection.setSelection(getAdapterPosition(channelArrayAdapter.getCount(), taskBo.getMode()));
            } else {
                channelwise_rb.setChecked(true);
                setUpSpinnerData(2);
                spinnerSelection.setSelection(getAdapterPosition(retailerMasterArrayAdapter.getCount(), taskBo.getMode()));
            }
        }
        taskView.setText(taskDataObj.getTaskDesc());

    }


    private int getAdapterPosition(int length, String mode) {
        for (int i = 0; i < length; i++) {
            switch (mode) {
                case TaskConstant.SELLER_WISE:
                    if (userMasterArrayAdapter.getItem(i).getUserid() == taskBo.getUserId())
                        return i;
                    break;
                case TaskConstant.RETAILER_WISE:
                    if (retailerMasterArrayAdapter.getItem(i).getTretailerId() == taskBo.getRid())
                        return i;
                    break;
                case TaskConstant.CHANNEL_WISE:
                    if (channelArrayAdapter.getItem(i).getChannelId() == taskBo.getChannelId())
                        return i;
                    break;
                default:
                    if (taskCategoryArrayAdapter.getItem(i).getTaskCategoryID() == taskBo.getTaskCategoryID())
                        return i;
                    break;
            }
        }
        return -1;
    }

}
