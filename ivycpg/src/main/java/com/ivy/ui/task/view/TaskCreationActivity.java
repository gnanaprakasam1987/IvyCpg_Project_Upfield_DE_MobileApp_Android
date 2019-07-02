package com.ivy.ui.task.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.constraint.Group;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.base.view.BaseActivity;
import com.ivy.cpg.view.homescreen.HomeScreenActivity;
import com.ivy.sd.camera.CameraActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.view.DataPickerDialogFragment;
import com.ivy.ui.task.TaskConstant;
import com.ivy.ui.task.TaskContract;
import com.ivy.ui.task.adapter.TaskImgListAdapter;
import com.ivy.ui.task.di.DaggerTaskComponent;
import com.ivy.ui.task.di.TaskModule;
import com.ivy.ui.task.model.TaskDataBO;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FileUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnItemSelected;

import static com.ivy.core.IvyConstants.DEFAULT_DATE_FORMAT;

public class TaskCreationActivity extends BaseActivity implements TaskContract.TaskCreationView, DataPickerDialogFragment.UpdateDateInterface {

    @Inject
    TaskContract.TaskPresenter<TaskContract.TaskView> taskPresenter;

    private int mSelectedCategoryID = 0;
    private boolean isRetailerWiseTask;
    private boolean isFromHomeSrc;
    private String menuCode;
    private int screenMode = 0;
    private TaskDataBO taskBo;
    private String screenTitle = "";
    private String mode = TaskConstant.SELLER_WISE;
    private int mSelectedSpinnerPos = 0, retSelectedPos = 0;
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

    @BindView(R.id.auto_complete_txt_retailer)
    AppCompatAutoCompleteTextView retSelectionAutoCompTxt;

    @BindView(R.id.switch_option)
    SwitchCompat switchOption;

    @BindView(R.id.option_txt)
    TextView optionTextView;

    @BindView(R.id.self_user)
    RadioButton selfUserRb;

    @BindView(R.id.peer_user)
    RadioButton peerUserRb;

    @BindView(R.id.link_user)
    RadioButton linkUserRb;

    @BindView(R.id.task_category_spinner)
    AppCompatSpinner categorySpinner;

    @BindView(R.id.task_category_tv)
    TextView productLevelTV;

    @BindView(R.id.parent_user)
    RadioButton parenUserRBtn;

    @BindView(R.id.child_user)
    RadioButton childUserRb;

    @BindView(R.id.saveTask)
    Button saveBtn;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.task_img_recycler_view)
    RecyclerView imgListRecyclerView;

    @BindView(R.id.task_due_date_btn)
    Button dueDateBtn;

    @BindView(R.id.product_level_group)
    Group productLevelGroup;

    private ArrayAdapter<UserMasterBO> parentUserMasterArrayAdapter;

    private ArrayAdapter<UserMasterBO> childUserMasterArrayAdapter;

    private ArrayAdapter<UserMasterBO> peerUserMasterArrayAdapter;

    private ArrayAdapter<UserMasterBO> linkUserMasterArrayAdapter;

    private ArrayAdapter<RetailerMasterBO> retailerMasterArrayAdapter;

    private ArrayAdapter<TaskDataBO> taskCategoryArrayAdapter;

    private HashMap<String, ArrayList<UserMasterBO>> linkUserListHashMap = new HashMap<>();

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
            isFromHomeSrc = getIntent().getExtras().getBoolean(TaskConstant.FROM_HOME_SCREEN, false);
            isRetailerWiseTask = getIntent().getExtras().getBoolean(TaskConstant.RETAILER_WISE_TASK, false);
            screenTitle = getIntent().getExtras().getString(TaskConstant.SCREEN_TITLE, getString(R.string.task_creation));
            screenMode = getIntent().getExtras().getInt(TaskConstant.TASK_SCREEN_MODE, TaskConstant.NEW_TASK_CREATION);
            taskBo = getIntent().getExtras().getParcelable(TaskConstant.TASK_OBJECT);
            menuCode = getIntent().getExtras().getString(TaskConstant.MENU_CODE, "MENU_TASK");
        }
    }

    @Override
    protected void setUpViews() {
        setUnBinder(ButterKnife.bind(this));
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        setUpToolBar();
        setUpRecyclerView();
        setImagePath();
        taskCreationDownloadMethods();

        if (isRetailerWiseTask) {
            setUpRetailerWiseMode();
        } else {
            mode = TaskConstant.SELLER_WISE;
        }
    }

    private void setUpRetailerWiseMode() {
        mode = TaskConstant.RETAILER_WISE;
        switchOption.setVisibility(View.GONE);
        optionTextView.setText(getString(R.string.retailer_wise));
        if (!isFromHomeSrc)
            retSelectionAutoCompTxt.setVisibility(View.GONE);
        else {
            retSelectionAutoCompTxt.setVisibility(View.VISIBLE);
        }
        peerUserRb.setVisibility(View.GONE);
    }


    private void taskCreationDownloadMethods() {
        taskPresenter.fetchLabels();

        setUpRetailerSelection();
        setUpAdapter();
        taskPresenter.fetchTaskCreationData(taskPresenter.getRetailerID(), taskBo == null ? "0" : taskBo.getTaskId());

        if (taskPresenter.isShowProdLevel()) {
            setUpCategoryAdapter();
            taskPresenter.fetchTaskCategory();
        }
    }

    private void setImagePath() {
        TaskConstant.TASK_SERVER_IMG_PATH = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/"
                + taskPresenter.getUserID()
                + DataMembers.DIGITAL_CONTENT + "/"
                + DataMembers.TASK_DIGITAL_CONTENT;
    }

    @Override
    public void setTaskRetailerListData(ArrayList<RetailerMasterBO> retailerList) {
        retailerMasterArrayAdapter.clear();
        retailerMasterArrayAdapter.add(new RetailerMasterBO("0", getString(R.string.select_retailer)));
        retailerMasterArrayAdapter.addAll(retailerList);
        retailerMasterArrayAdapter.notifyDataSetChanged();
        retSelectionAutoCompTxt.setAdapter(retailerMasterArrayAdapter);
    }

    @Override
    public void setParentUserListData(ArrayList<UserMasterBO> userList) {
        if (!userList.isEmpty()) {
            parentUserMasterArrayAdapter.clear();
            parentUserMasterArrayAdapter.add(new UserMasterBO(0, getString(R.string.select_seller)));
            parentUserMasterArrayAdapter.addAll(userList);
            parentUserMasterArrayAdapter.notifyDataSetChanged();
            spinnerSelection.setAdapter(parentUserMasterArrayAdapter);
        } else {
            parenUserRBtn.setVisibility(View.GONE);
        }

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
    public void setChildUserListData(ArrayList<UserMasterBO> childUserList) {
        if (!childUserList.isEmpty()) {
            childUserMasterArrayAdapter.clear();
            childUserMasterArrayAdapter.add(new UserMasterBO(0, getString(R.string.select_child)));
            childUserMasterArrayAdapter.addAll(childUserList);
            childUserMasterArrayAdapter.notifyDataSetChanged();
        } else {
            childUserRb.setVisibility(View.GONE);
        }
    }

    @Override
    public void setPeerUserListData(ArrayList<UserMasterBO> peerUserList) {
        if (!peerUserList.isEmpty()) {
            peerUserMasterArrayAdapter.clear();
            peerUserMasterArrayAdapter.add(new UserMasterBO(0, getString(R.string.select_peer)));
            peerUserMasterArrayAdapter.addAll(peerUserList);
            peerUserMasterArrayAdapter.notifyDataSetChanged();
        } else {
            peerUserRb.setVisibility(View.GONE);
        }
    }

    @Override
    public void setLinkUserListData(HashMap<String, ArrayList<UserMasterBO>> linkUserListMap) {
        if (!linkUserListMap.isEmpty()) {
            linkUserListHashMap.clear();
            linkUserListHashMap.putAll(linkUserListMap);
        } else {
            handleLinkUserVisibility(View.GONE);
        }
    }

    private void setUpLinkUserAdapter(String retailerId) {
        linkUserMasterArrayAdapter.clear();
        linkUserMasterArrayAdapter.add(new UserMasterBO(0, getString(R.string.select_link)));

        if (linkUserListHashMap.get(retailerId) != null)
            linkUserMasterArrayAdapter.addAll(linkUserListHashMap.get(retailerId));

        linkUserMasterArrayAdapter.notifyDataSetChanged();
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

            if (screenMode == TaskConstant.EDIT_MODE_FROM_TASK_DETAIL_SRC) {
                setResult(TaskConstant.TASK_CREATED_SUCCESS_CODE,
                        new Intent(TaskCreationActivity.this,
                                TaskDetailActivity.class));
                finish();
                overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            } else
                backNavigation(TaskConstant.TASK_CREATED_SUCCESS_CODE);
        });
    }

    @Override
    public void showTaskDueDateError() {
        showMessage(R.string.select_due_date);
    }

    @Override
    public void showSpinnerSelectionError() {
        showMessage(getString(R.string.plain_select) + " " + getTaskMode());
    }

    @Override
    public void showRetSelectionError() {
        showMessage(getString(R.string.select_retailer));
    }

    @Override
    public void updateImageListAdapter(ArrayList<TaskDataBO> imageList) {
        imgListRecyclerView.setAdapter(new TaskImgListAdapter(TaskCreationActivity.this, imageList, false, photoClickListener));
    }

    @Override
    public void showImageUpdateMsg() {
        showMessage(R.string.image_saved);
    }

    @Override
    public void onDeleteSuccess() {

    }

    @Override
    public void showErrorMsg() {
        showMessage(getString(R.string.something_went_wrong));
    }

    TaskImgListAdapter.PhotoClickListener photoClickListener = this::prepareTaskPhotoCapture;

    @Override
    public String getTaskMode() {
        return mode;
    }

    @OnCheckedChanged({R.id.switch_option})
    public void onChoiceCheckChangeListener(SwitchCompat switchBtn) {
        spinnerSelection.setVisibility(View.GONE);

        if (switchBtn.isChecked()) {
            switchRetailerWise();
        } else {
            switchSellerWise();
        }
    }

    private void switchSellerWise() {
        mode = getTaskMode();
        retSelectionAutoCompTxt.setVisibility(View.GONE);
        optionTextView.setText(getString(R.string.Seller));
        peerUserRb.setVisibility(View.VISIBLE);
        handleLinkUserVisibility(View.GONE);
        handleProductLevelVisibility(View.GONE);
    }

    private void switchRetailerWise() {
        mode = TaskConstant.RETAILER_WISE;
        optionTextView.setText(getString(R.string.retailer_wise));
        retSelectionAutoCompTxt.setVisibility(View.VISIBLE);
        if (!linkUserMasterArrayAdapter.isEmpty())
            handleLinkUserVisibility(View.VISIBLE);

        handleProductLevelVisibility(View.VISIBLE);
        peerUserRb.setVisibility(View.GONE);
    }

    @OnClick({R.id.self_user, R.id.parent_user, R.id.child_user, R.id.peer_user, R.id.link_user})
    public void onTaskCheckChangedListener(RadioButton radioBtn) {

        switch (radioBtn.getId()) {
            case R.id.self_user:
                updateOptionUnSelectedTxtColor();
                selfUserRb.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
                mode = TaskConstant.SELLER_WISE;
                spinnerSelection.setVisibility(View.GONE);
                break;

            case R.id.parent_user:
                updateOptionUnSelectedTxtColor();
                parenUserRBtn.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
                mode = TaskConstant.PARENT_WISE;
                setUpSpinnerData(0);
                break;

            case R.id.child_user:
                updateOptionUnSelectedTxtColor();
                childUserRb.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
                mode = TaskConstant.CHILD_WISE;
                setUpSpinnerData(1);
                break;


            case R.id.peer_user:
                updateOptionUnSelectedTxtColor();
                peerUserRb.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
                mode = TaskConstant.PEERT_WISE;
                setUpSpinnerData(2);
                break;

            case R.id.link_user:
                updateOptionUnSelectedTxtColor();
                linkUserRb.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
                setUpSpinnerData(3);
                break;


        }
    }

    private void updateOptionUnSelectedTxtColor() {

        selfUserRb.setTextColor(ContextCompat.getColor(this, R.color.textColorPrimary));
        childUserRb.setTextColor(ContextCompat.getColor(this, R.color.textColorPrimary));
        parenUserRBtn.setTextColor(ContextCompat.getColor(this, R.color.textColorPrimary));
        peerUserRb.setTextColor(ContextCompat.getColor(this, R.color.textColorPrimary));
        linkUserRb.setTextColor(ContextCompat.getColor(this, R.color.textColorPrimary));
    }


    @OnItemSelected(R.id.spinner_seller)
    public void onUserSpinnerSelected(Spinner spinner, int position) {
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
        hideKeyboard();
        DataPickerDialogFragment newFragment = new DataPickerDialogFragment();
        newFragment.show(getSupportFragmentManager(), TAG_DATE_PICKER_TO);
    }

    @Override
    public void updateLabelNames(HashMap<String, String> labelMap) {
        if (labelMap.containsKey(TaskConstant.TASK_TITLE_LABEL))
            ((TextView) findViewById(R.id.task_title_tv)).setText(labelMap.get(TaskConstant.TASK_TITLE_LABEL));

        if (labelMap.containsKey(TaskConstant.TASK_DUE_DATE_LABEL))
            ((TextView) findViewById(R.id.task_due_date_tv)).setText(labelMap.get(TaskConstant.TASK_DUE_DATE_LABEL));

        if (labelMap.containsKey(TaskConstant.TASK_CREATED_BY_LABEL))
            productLevelTV.setText(labelMap.get(TaskConstant.TASK_CREATED_BY_LABEL));

        if (labelMap.containsKey(TaskConstant.TASK_APPLICABLE_FOR_LABEL))
            ((TextView) findViewById(R.id.applicable_tv)).setText(TaskConstant.TASK_APPLICABLE_FOR_LABEL);

        if (labelMap.containsKey(TaskConstant.TASK_PHOTO_CAPTURE_LABEL))
            ((TextView) findViewById(R.id.photo_capture_tv)).setText(TaskConstant.TASK_PHOTO_CAPTURE_LABEL);

        if (labelMap.containsKey(TaskConstant.TASK_DESCRIPTION_LABEL))
            ((TextView) findViewById(R.id.task_desc_tv)).setText(TaskConstant.TASK_DESCRIPTION_LABEL);
    }

    @Override
    public void updateListData(ArrayList<TaskDataBO> updatedList) {
        if (screenMode == TaskConstant.EDIT_MODE_FROM_TASK_DETAIL_SRC
                || screenMode == TaskConstant.EDIT_MODE_FROM_TASK_FRAGMENT_SRC) {
            updatedList.add(0, new TaskDataBO());
            taskPresenter.getTaskImgList();
            imgListRecyclerView.setAdapter(new TaskImgListAdapter(TaskCreationActivity.this, updatedList, false, photoClickListener));
            updateFieldsInEditMode(taskBo);
        } else {
            taskPresenter.addNewImage("");
        }
    }

    @OnClick(R.id.saveTask)
    public void onSaveClickBtn() {
        int taskAssignId = 0, linkUserId = 0, retSelectionId = -1;
        String taskDetailDesc = taskView.getText().toString();
        String taskTitleDec = taskTitle.getText().toString();
        String taskDuedate = dueDateBtn.getText().toString().isEmpty() ? null
                : dueDateBtn.getText().toString();

        if (!isRetailerWiseTask
                && retSelectionAutoCompTxt.getText().toString()
                .equalsIgnoreCase(getString(R.string.select_retailer)))
            retSelectionId = 0;

        taskAssignId = mSelectedSpinnerPos == 0 ? -1 : mSelectedSpinnerPos;

        if (!taskPresenter.validate(taskTitle.getText().toString(), taskView.getText().toString(), taskDuedate, retSelectionId, taskAssignId))
            return;

        if (!switchOption.isChecked()
                && switchOption.getVisibility() == View.VISIBLE) {
            taskAssignId = getSelectedUserId();
        } else {
            mode = TaskConstant.RETAILER_WISE;
            if (isFromHomeSrc) {
                taskAssignId = (taskBo != null && retSelectedPos == 0) ? taskBo.getRid() : SDUtil.convertToInt(retailerMasterArrayAdapter.getItem(retSelectedPos).getRetailerID());
            } else
                taskAssignId = taskPresenter.getRetailerID();

            linkUserId = getSelectedUserId();
        }

        if (screenMode == TaskConstant.NEW_TASK_CREATION)
            taskBo = new TaskDataBO();

        taskBo.setTasktitle(taskTitleDec);
        taskBo.setTaskDesc(taskDetailDesc);
        taskBo.setTaskDueDate(taskDuedate);
        taskBo.setTaskCategoryID(mSelectedCategoryID);
        taskBo.setMode(mode);

        taskPresenter.onSaveTask(taskAssignId, taskBo, linkUserId, retSelectionId);
    }

    private int getSelectedUserId() {

        if (selfUserRb.isChecked())
            return taskPresenter.getUserID();
        else if (parenUserRBtn.isChecked())
            return parentUserMasterArrayAdapter.getItem(mSelectedSpinnerPos).getUserid();
        else if (childUserRb.isChecked())
            return childUserMasterArrayAdapter.getItem(mSelectedSpinnerPos).getUserid();
        else if (peerUserRb.isChecked())
            return peerUserMasterArrayAdapter.getItem(mSelectedSpinnerPos).getUserid();
        else
            return linkUserMasterArrayAdapter.getItem(mSelectedSpinnerPos).getUserid();

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

    private void setUpRetailerSelection() {
        retailerMasterArrayAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_blacktext_layout);
        retailerMasterArrayAdapter.setDropDownViewResource(R.layout.spinner_blacktext_list_item);

        retSelectionAutoCompTxt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                retSelectionAutoCompTxt.showDropDown();
                return false;
            }
        });

        retSelectionAutoCompTxt.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                retSelectedPos = position;
                if (!retailerMasterArrayAdapter.getItem(position).getRetailerID().equals("0")) {
                    setUpLinkUserAdapter(retailerMasterArrayAdapter.getItem(position).getRetailerID());
                }
            }
        });
    }

    private void setUpAdapter() {
        parentUserMasterArrayAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_blacktext_layout);
        parentUserMasterArrayAdapter.setDropDownViewResource(R.layout.spinner_blacktext_list_item);

        childUserMasterArrayAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_blacktext_layout);
        childUserMasterArrayAdapter.setDropDownViewResource(R.layout.spinner_blacktext_list_item);

        peerUserMasterArrayAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_blacktext_layout);
        peerUserMasterArrayAdapter.setDropDownViewResource(R.layout.spinner_blacktext_list_item);

        linkUserMasterArrayAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_blacktext_layout);
        linkUserMasterArrayAdapter.setDropDownViewResource(R.layout.spinner_blacktext_list_item);

    }

    private void setUpSpinnerData(int isFrom) {
        mSelectedSpinnerPos = 0;
        spinnerSelection.setVisibility(View.VISIBLE);
        switch (isFrom) {
            case 0:
                spinnerSelection.setAdapter(parentUserMasterArrayAdapter);
                break;
            case 1:
                spinnerSelection.setAdapter(childUserMasterArrayAdapter);
                break;
            case 2:
                spinnerSelection.setAdapter(peerUserMasterArrayAdapter);
                break;
            case 3:
                spinnerSelection.setAdapter(linkUserMasterArrayAdapter);
                break;
        }
    }

    private void handleProductLevelVisibility(int visibility) {

        if (!taskPresenter.isShowProdLevel()) {
            productLevelGroup.setVisibility(View.GONE);
        } else {
            productLevelGroup.setVisibility(visibility);
            categorySpinner.setSelection(0);
        }
    }

    private void handleLinkUserVisibility(int visibility) {
        linkUserRb.setVisibility(visibility);
    }

    private void setUpCategoryAdapter() {
        taskCategoryArrayAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_blacktext_layout);
        taskCategoryArrayAdapter.setDropDownViewResource(R.layout.spinner_blacktext_list_item);
    }

    private void prepareTaskPhotoCapture() {

        imageName = "TSK_" + taskPresenter.getUserID()
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

    private void backNavigation(int resultCode) {

        if (resultCode != -1) {

            if (isFromHomeSrc)
                setResult(resultCode, new Intent(TaskCreationActivity.this,
                        HomeScreenActivity.class).putExtra(TaskConstant.MENU_CODE, menuCode));
            else
                setResult(resultCode, new Intent(TaskCreationActivity.this,
                        TaskActivity.class).putExtra(TaskConstant.RETAILER_WISE_TASK, isRetailerWiseTask)
                        .putExtra(TaskConstant.SCREEN_TITLE, screenTitle));
        }
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

            backNavigation(-1);
        }, () -> {

        });
    }

    private void updateFieldsInEditMode(@NotNull TaskDataBO taskDataObj) {
        taskTitle.setText(taskDataObj.getTasktitle());
        if (isRetailerWiseTask)
            categorySpinner.setSelection(getAdapterPosition(TaskConstant.PRODUCT_LEVEL_WISE));

        dueDateBtn.setText(DateTimeUtils.convertFromServerDateToRequestedFormat(
                taskDataObj.getTaskDueDate(), taskPresenter.outDateFormat()));

        if (taskDataObj.getRid() != 0)
            updateRetailerSelection();

        switch (taskDataObj.getMode()) {

            case TaskConstant.SELLER_WISE:
                selfUserRb.setChecked(true);
                break;

            case TaskConstant.PARENT_WISE:
                parenUserRBtn.setChecked(true);
                setUpSpinnerData(0);
                spinnerSelection.setSelection(getAdapterPosition(taskBo.getMode()));
                break;

            case TaskConstant.CHILD_WISE:
                childUserRb.setChecked(true);
                setUpSpinnerData(1);
                spinnerSelection.setSelection(getAdapterPosition(taskBo.getMode()));
                break;

            case TaskConstant.PEERT_WISE:
                peerUserRb.setVisibility(View.VISIBLE);
                peerUserRb.setChecked(true);
                setUpSpinnerData(2);
                spinnerSelection.setSelection(getAdapterPosition(taskBo.getMode()));
                break;

            case TaskConstant.RETAILER_WISE:
                if (isFromHomeSrc)
                    retSelectionAutoCompTxt.setText(retailerMasterArrayAdapter.getItem(getAdapterPosition(taskBo.getMode())).getRetailerName());

                if (!linkUserMasterArrayAdapter.isEmpty()) {
                    handleLinkUserVisibility(View.VISIBLE);
                    linkUserRb.setChecked(true);
                    setUpSpinnerData(3);
                    spinnerSelection.setSelection(getAdapterPosition(TaskConstant.LINK_WISE));
                }
                break;

        }
        taskView.setText(taskDataObj.getTaskDesc());

    }

    private void updateRetailerSelection() {
        if (isFromHomeSrc)
            retSelectionAutoCompTxt.setText(retailerMasterArrayAdapter.getItem(getAdapterPosition(TaskConstant.RETAILER_WISE)).getRetailerName());

        if (!linkUserMasterArrayAdapter.isEmpty()) {
            handleLinkUserVisibility(View.VISIBLE);
            linkUserRb.setChecked(true);
            setUpSpinnerData(3);
            spinnerSelection.setSelection(getAdapterPosition(TaskConstant.LINK_WISE));
        }
    }

    private int getAdapterPosition(String mode) {
        ArrayAdapter dummyArrayAdapter;

        switch (mode) {
            case TaskConstant.PARENT_WISE:
                dummyArrayAdapter = parentUserMasterArrayAdapter;
                break;
            case TaskConstant.CHILD_WISE:
                dummyArrayAdapter = childUserMasterArrayAdapter;
                break;
            case TaskConstant.PEERT_WISE:
                dummyArrayAdapter = childUserMasterArrayAdapter;
                break;
            case TaskConstant.RETAILER_WISE:
                dummyArrayAdapter = retailerMasterArrayAdapter;
                break;
            case TaskConstant.LINK_WISE:
                dummyArrayAdapter = linkUserMasterArrayAdapter;
                break;
            default:
                dummyArrayAdapter = taskCategoryArrayAdapter;
                break;
        }

        for (int i = 0; i < dummyArrayAdapter.getCount(); i++) {

            if (mode.equalsIgnoreCase(TaskConstant.RETAILER_WISE)) {
                if (SDUtil.convertToInt(((RetailerMasterBO) dummyArrayAdapter.getItem(i)).getRetailerID()) == taskBo.getRid())
                    return i;
            } else if (mode.equalsIgnoreCase(TaskConstant.PRODUCT_LEVEL_WISE)) {
                if (((TaskDataBO) dummyArrayAdapter.getItem(i)).getTaskCategoryID() == taskBo.getTaskCategoryID())
                    return i;
            } else {
                if (((UserMasterBO) dummyArrayAdapter.getItem(i)).getUserid() == taskBo.getUserId())
                    return i;
            }
        }
        return -1;
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
                backNavigation(-1);


            return true;
        }
        return false;
    }

}
