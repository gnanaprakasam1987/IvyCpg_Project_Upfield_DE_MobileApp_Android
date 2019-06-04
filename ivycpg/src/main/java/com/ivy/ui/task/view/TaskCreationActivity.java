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
import com.ivy.utils.AppUtils;
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
    Spinner parentSpinner;

    @BindView(R.id.auto_complete_txt_retailer)
    AppCompatAutoCompleteTextView retSelectionAutoCompTxt;

    @BindView(R.id.spinner_child)
    Spinner childSpinner;

    @BindView(R.id.spinner_peer)
    Spinner peerSpinner;

    @BindView(R.id.spinner_link)
    Spinner linkSpinner;

    @BindView(R.id.seller_selection)
    RadioButton sellerSelectionRb;

    @BindView(R.id.retailer_selection)
    RadioButton retailerSelectionRb;

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

    @BindView(R.id.retailer_wise)
    RadioButton retailerwise_rb;

    @BindView(R.id.saveTask)
    Button saveBtn;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.task_img_recycler_view)
    RecyclerView imgListRecyclerView;

    @BindView(R.id.task_due_date_btn)
    Button dueDateBtn;

    @BindView(R.id.retailer_group)
    Group retailerGroup;

    @BindView(R.id.peer_group)
    Group peerGroup;

    @BindView(R.id.link_group)
    Group linkGroup;

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
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        setUpToolBar();
        setUpRecyclerView();
        setUpRetailerSelection();
        setUpAdapter();
        TaskConstant.TASK_SERVER_IMG_PATH = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/"
                + taskPresenter.getUserID()
                + DataMembers.DIGITAL_CONTENT + "/"
                + DataMembers.TASK_DIGITAL_CONTENT;

        //allow only create task only for retailer if not from seller Task
        taskPresenter.fetchData(taskPresenter.getRetailerID());

        if (isRetailerWiseTask) {
            handleViewVisibility(View.VISIBLE);
        } else {
            handleViewVisibility(View.INVISIBLE);
        }

        setUpCategoryAdapter();
        taskPresenter.fetchTaskCategory();


        if (screenMode == 1)
            taskPresenter.fetchTaskImageList(taskBo.getTaskId());
        else
            taskPresenter.addNewImage("");
    }


    private void handleViewVisibility(int visibilityState) {
        //  radioGroup.setVisibility(visibilityState);
        parentSpinner.setVisibility(visibilityState);
        mode = "retailer";
    }

    @Override
    public void setTaskRetailerListData(ArrayList<RetailerMasterBO> retailerList) {
        retailerMasterArrayAdapter.clear();
        retailerMasterArrayAdapter.add(new RetailerMasterBO(0, getString(R.string.all_retailer)));
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
            parentSpinner.setAdapter(parentUserMasterArrayAdapter);
        } else {
            parenUserRBtn.setVisibility(View.GONE);
            parentSpinner.setVisibility(View.GONE);
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
            childSpinner.setAdapter(childUserMasterArrayAdapter);
        } else {
            childUserRb.setVisibility(View.GONE);
            childSpinner.setVisibility(View.GONE);
        }
    }

    @Override
    public void setPeerUserListData(ArrayList<UserMasterBO> peerUserList) {
        if (!peerUserList.isEmpty()) {
            peerUserMasterArrayAdapter.clear();
            peerUserMasterArrayAdapter.add(new UserMasterBO(0, getString(R.string.select_peer)));
            peerUserMasterArrayAdapter.addAll(peerUserList);
            peerUserMasterArrayAdapter.notifyDataSetChanged();
            peerSpinner.setAdapter(peerUserMasterArrayAdapter);
        } else {
            peerGroup.setVisibility(View.GONE);
        }
    }

    @Override
    public void setLinkUserListData(HashMap<String, ArrayList<UserMasterBO>> linkUserListMap) {
        if (!linkUserListMap.isEmpty()) {
            linkUserListHashMap.clear();
            linkUserListHashMap.putAll(linkUserListMap);
        } else {
            linkGroup.setVisibility(View.GONE);
        }
    }

    private void setUpLinkUserAdapter(String retailerId) {
        linkUserMasterArrayAdapter.clear();
        linkUserMasterArrayAdapter.add(new UserMasterBO(0, getString(R.string.select_link)));
        linkUserMasterArrayAdapter.addAll(linkUserListHashMap.get(retailerId));
        linkUserMasterArrayAdapter.notifyDataSetChanged();
        linkSpinner.setAdapter(linkUserMasterArrayAdapter);
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
                        HomeScreenActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        .putExtra(TaskConstant.MENU_CODE, menuCode));
            else
                startActivity(new Intent(TaskCreationActivity.this,
                        TaskActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        .putExtra(TaskConstant.RETAILER_WISE_TASK, isRetailerWiseTask)
                        .putExtra(TaskConstant.SCREEN_TITLE, screenTitle));
            finish();
            overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
        });
    }

    @Override
    public void showTaskDueDateError() {

    }

    @Override
    public void showLinkUserError() {

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

    }

    TaskImgListAdapter.PhotoClickListener photoClickListener = this::prepareTaskPhotoCapture;

    @Override
    public String getTaskMode() {
        return mode;
    }


    @OnClick({R.id.seller_selection, R.id.retailer_selection})
    public void onChoiceCheckChangeListener(RadioButton radioBtn) {
        switch (radioBtn.getId()) {

            case R.id.seller_selection:
                retailerSelectionRb.setChecked(false);
                handleSellerSelectionVisibility();
                break;
            case R.id.retailer_selection:
                sellerSelectionRb.setChecked(false);
                handleRetSelectionVisibility();
                break;
        }
    }

    @OnClick({R.id.self_user, R.id.parent_user, R.id.child_user, R.id.retailer_wise, R.id.peer_user, R.id.link_user})
    public void onTaskCheckChangedListener(RadioButton radioBtn) {

        switch (radioBtn.getId()) {
            case R.id.self_user:
                parenUserRBtn.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
                childUserRb.setTextColor(ContextCompat.getColor(this, R.color.textColorPrimary));
                retailerwise_rb.setTextColor(ContextCompat.getColor(this, R.color.textColorPrimary));
                mode = "seller";
                hideSpinnerVisibility(View.GONE);
                break;

            case R.id.parent_user:
                parenUserRBtn.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
                childUserRb.setTextColor(ContextCompat.getColor(this, R.color.textColorPrimary));
                retailerwise_rb.setTextColor(ContextCompat.getColor(this, R.color.textColorPrimary));
                mode = "parent";
                hideSpinnerVisibility(View.GONE);
                unCheckedSelfRB();
                parentSpinner.setVisibility(View.VISIBLE);
                break;

            case R.id.child_user:
                parenUserRBtn.setTextColor(ContextCompat.getColor(this, R.color.textColorPrimary));
                childUserRb.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
                retailerwise_rb.setTextColor(ContextCompat.getColor(this, R.color.textColorPrimary));
                mode = "child";
                hideSpinnerVisibility(View.GONE);
                unCheckedSelfRB();
                childSpinner.setVisibility(View.VISIBLE);
                break;

            case R.id.retailer_wise:
                parenUserRBtn.setTextColor(ContextCompat.getColor(this, R.color.textColorPrimary));
                childUserRb.setTextColor(ContextCompat.getColor(this, R.color.textColorPrimary));
                retailerwise_rb.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
                mode = "retailer";
                hideSpinnerVisibility(View.GONE);
                retSelectionAutoCompTxt.setVisibility(View.VISIBLE);
                break;

            case R.id.peer_user:
                parenUserRBtn.setTextColor(ContextCompat.getColor(this, R.color.textColorPrimary));
                childUserRb.setTextColor(ContextCompat.getColor(this, R.color.textColorPrimary));
                retailerwise_rb.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
                mode = "peer";
                hideSpinnerVisibility(View.GONE);
                unCheckedSelfRB();
                peerSpinner.setVisibility(View.VISIBLE);
                break;

            case R.id.link_user:
                parenUserRBtn.setTextColor(ContextCompat.getColor(this, R.color.textColorPrimary));
                childUserRb.setTextColor(ContextCompat.getColor(this, R.color.textColorPrimary));
                retailerwise_rb.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
                hideSpinnerVisibility(View.GONE);
                linkSpinner.setVisibility(View.VISIBLE);
                break;


        }
    }


    private void unCheckedSelfRB(){
        if (selfUserRb.isChecked())
            selfUserRb.setChecked(false);
    }

    private void hideSpinnerVisibility(int visibility) {

        if (parentSpinner.getVisibility() == View.VISIBLE) {
            parenUserRBtn.setChecked(false);
            parentSpinner.setVisibility(View.INVISIBLE);
            if (!parentUserMasterArrayAdapter.isEmpty())
                parentSpinner.setSelection(0);
        }

        if (childSpinner.getVisibility() == View.VISIBLE) {
            childUserRb.setChecked(false);
            childSpinner.setVisibility(View.INVISIBLE);
            if (!childUserMasterArrayAdapter.isEmpty())
                childSpinner.setSelection(0);
        }

        if (peerSpinner.getVisibility() == View.VISIBLE) {
            peerUserRb.setChecked(false);
            peerSpinner.setVisibility(visibility);
            if (!childUserMasterArrayAdapter.isEmpty())
                childSpinner.setSelection(0);
        }

        if (linkSpinner.getVisibility() == View.VISIBLE) {
            linkUserRb.setChecked(false);
            linkSpinner.setVisibility(visibility);
            if (!linkUserMasterArrayAdapter.isEmpty())
                linkSpinner.setSelection(0);
        }

        if (retSelectionAutoCompTxt.getVisibility() == View.VISIBLE) {
            retailerSelectionRb.setChecked(false);
            retSelectionAutoCompTxt.setVisibility(visibility);
            if (!retailerMasterArrayAdapter.isEmpty())
                retSelectionAutoCompTxt.setSelection(0);
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

    @OnItemSelected(R.id.spinner_child)
    public void onChildSpinnerSelected(Spinner spinner, int position) {
        ((TextView) spinner.getSelectedView().findViewById(android.R.id.text1)).setGravity(Gravity.START);
    }

    @OnItemSelected(R.id.spinner_peer)
    public void onPeerSpinnerSelected(Spinner spinner, int position) {
        ((TextView) spinner.getSelectedView().findViewById(android.R.id.text1)).setGravity(Gravity.START);
    }

    @OnItemSelected(R.id.spinner_link)
    public void onLinkSpinnerSelected(Spinner spinner, int position) {
        ((TextView) spinner.getSelectedView().findViewById(android.R.id.text1)).setGravity(Gravity.START);
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
        int taskCreatedId = 0, linkUserId = 0;
        if (!taskPresenter.validate(taskTitle.getText().toString(), taskView.getText().toString(), taskDuedate))
            return;

        switch (mode) {
            case TaskConstant.SELLER_WISE:
                taskCreatedId = taskPresenter.getUserID();
                break;

            case TaskConstant.PARENT_WISE:
                taskCreatedId = ((UserMasterBO) parentSpinner.getSelectedItem()).getUserid();
                break;


            case TaskConstant.CHILD_WISE:
                taskCreatedId = ((UserMasterBO) childSpinner.getSelectedItem()).getUserid();
                break;

            case TaskConstant.RETAILER_WISE:
                if (!isRetailerWiseTask)
                    taskCreatedId = SDUtil.convertToInt(retailerMasterArrayAdapter.getItem(mSelectedSpinnerPos).getRetailerID());
                else
                    taskCreatedId = taskPresenter.getRetailerID();

                if (linkSpinner != null)
                    linkUserId = ((UserMasterBO) linkSpinner.getSelectedItem()).getUserid();
                break;

            case TaskConstant.PEERT_WISE:
                taskCreatedId = ((UserMasterBO) peerSpinner.getSelectedItem()).getUserid();
                break;
        }

        if (screenMode == 0)
            taskBo = new TaskDataBO();

        taskBo.setTasktitle(taskTitleDec);
        taskBo.setTaskDesc(taskDetailDesc);
        taskBo.setTaskDueDate(taskDuedate);
        taskBo.setTaskCategoryID(mSelectedCategoryID);

        taskPresenter.onSaveButtonClick(taskCreatedId, taskBo, linkUserId);
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

                setUpLinkUserAdapter(retailerMasterArrayAdapter.getItem(position).getRetailerID());
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

    }

    private void handleSellerSelectionVisibility() {
        if (productLevelGroup.getVisibility() == View.VISIBLE) {
            productLevelGroup.setVisibility(View.GONE);
            categorySpinner.setSelection(0);
        }

        if (retailerwise_rb.getVisibility() == View.VISIBLE) {
            retailerGroup.setVisibility(View.GONE);
            retSelectionAutoCompTxt.setSelection(0);
        }

        if (linkGroup.getVisibility() == View.VISIBLE) {
            linkGroup.setVisibility(View.GONE);
            linkSpinner.setSelection(0);
        }

        if (!peerUserMasterArrayAdapter.isEmpty()) {
            peerGroup.setVisibility(View.VISIBLE);
            peerSpinner.setVisibility(View.INVISIBLE);
            peerSpinner.setSelection(0);
        }
    }

    private void handleRetSelectionVisibility() {
        if (taskPresenter.isShowProdLevel()) {
            productLevelGroup.setVisibility(View.VISIBLE);
            categorySpinner.setSelection(0);
        }

        retailerGroup.setVisibility(View.VISIBLE);
        retSelectionAutoCompTxt.setVisibility(View.INVISIBLE);
        retSelectionAutoCompTxt.setSelection(0);

        if (linkGroup.getVisibility() == View.GONE) {
            linkGroup.setVisibility(View.VISIBLE);
            linkSpinner.setVisibility(View.INVISIBLE);
            if (!linkUserMasterArrayAdapter.isEmpty())
                linkSpinner.setSelection(0);
        }

        if (peerUserRb.getVisibility() == View.VISIBLE)
            peerGroup.setVisibility(View.GONE);
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
                parenUserRBtn.setChecked(true);
                setUpSpinnerData(0);
                parentSpinner.setSelection(getAdapterPosition(parentUserMasterArrayAdapter.getCount(), taskBo.getMode()));
            } else if (taskDataObj.getMode().equalsIgnoreCase(TaskConstant.RETAILER_WISE)) {
                retailerwise_rb.setChecked(true);
                setUpSpinnerData(1);
                parentSpinner.setSelection(getAdapterPosition(childUserMasterArrayAdapter.getCount(), taskBo.getMode()));
            } else {
                childUserRb.setChecked(true);
                setUpSpinnerData(2);
                parentSpinner.setSelection(getAdapterPosition(retailerMasterArrayAdapter.getCount(), taskBo.getMode()));
            }
        }
        taskView.setText(taskDataObj.getTaskDesc());

    }


    private int getAdapterPosition(int length, String mode) {
        for (int i = 0; i < length; i++) {
            switch (mode) {
                case TaskConstant.SELLER_WISE:
                    if (parentUserMasterArrayAdapter.getItem(i).getUserid() == taskBo.getUserId())
                        return i;
                    break;
                case TaskConstant.RETAILER_WISE:
                    if (SDUtil.convertToInt(retailerMasterArrayAdapter.getItem(i).getRetailerID()) == taskBo.getRid())
                        return i;
                    break;
                case TaskConstant.CHILD_WISE:
                    if (childUserMasterArrayAdapter.getItem(i).getUserid() == taskBo.getUserId())
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

}
