package com.ivy.ui.retailer.filter.view;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.ivy.core.base.view.BaseBottomSheetDialogFragment;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.AttributeBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.ui.retailer.filter.FilterObjectBo;
import com.ivy.ui.retailer.filter.RetailerPlanFilterBo;
import com.ivy.ui.retailer.filter.RetailerPlanFilterContract;
import com.ivy.ui.retailer.filter.di.DaggerRetailerPlanFilterComponent;
import com.ivy.ui.retailer.filter.di.RetailerPlanFilterModule;
import com.ivy.ui.retailer.filter.presenter.RetailerPlanFilterPresenterImpl;
import com.ivy.utils.DeviceUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;

import static com.ivy.ui.retailer.filter.RetailerPlanFilterConstants.CODE_IS_NOT_VISITED;
import static com.ivy.ui.retailer.filter.RetailerPlanFilterConstants.CODE_LAST_VISIT_DATE;
import static com.ivy.ui.retailer.filter.RetailerPlanFilterConstants.CODE_TASK_DUE_DATE;

@SuppressLint("ValidFragment")
public class RetailerPlanFilterFragment extends BaseBottomSheetDialogFragment
        implements RetailerPlanFilterContract.RetailerPlanFilterView,DatePickerViewDialog.DateSelectListener {

    private BottomSheetBehavior bottomSheetBehavior;

    AppCompatCheckBox notVisitedCheckBox;

    TextView taskFromDate;

    TextView taskToDate;

    TextView lastVisitFromDate;

    TextView lastVisitToDate;

    @BindView(R.id.dynamic_layout)
    LinearLayout dynamicViewLayout;

    @Inject
    RetailerPlanFilterPresenterImpl<RetailerPlanFilterContract.RetailerPlanFilterView> presenter;

    private RetailerPlanFilterBo planFilterBo;

    public RetailerPlanFilterFragment(RetailerPlanFilterBo planFilterBo){
        this.planFilterBo = planFilterBo;
    }

    @Override
    public void initializeDi(){
        DaggerRetailerPlanFilterComponent.builder()
                .ivyAppComponent(((BusinessModel) Objects.requireNonNull((FragmentActivity)context).getApplication()).getComponent())
                .retailerPlanFilterModule(new RetailerPlanFilterModule(this))
                .build()
                .inject(this);

        setBasePresenter(presenter);
    }

    @Override
    protected int setContentViewLayout() {
        return R.layout.retailer_plan_filter_fragment;
    }

    @Override
    public void initVariables(Dialog dialog,View view) {

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) view.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();

        if (behavior != null && behavior instanceof BottomSheetBehavior) {

            bottomSheetBehavior = ((BottomSheetBehavior) behavior);

            bottomSheetBehavior.setHideable(true);

            bottomSheetBehavior.setPeekHeight(DeviceUtils.getDisplayMetrics(context).heightPixels);

            ((BottomSheetBehavior) behavior).setState(BottomSheetBehavior.STATE_EXPANDED);

            ((BottomSheetBehavior) behavior).setBottomSheetCallback(bottomSheetCallBack);
        }

        presenter.prepareConfiguration();
    }

    public void dateSelectListener(TextView textView,String minimumDate){

        String selectedDate = textView.getText().toString().equalsIgnoreCase(getString(R.string.select_date))?"":textView.getText().toString();
        minimumDate = minimumDate.equalsIgnoreCase(getString(R.string.select_date))?"":minimumDate;

        showDatePicker(selectedDate,minimumDate,textView);
    }

    @OnClick(R.id.close)
    void closeIconClick(){
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    @OnClick(R.id.clear_btn)
    void clearButtonListener(){

        if (planFilterBo != null)
            planFilterBo = new RetailerPlanFilterBo();

        if (presenter.isConfigureAvail(CODE_IS_NOT_VISITED))
            notVisitedCheckBox.setChecked(false);

        if (presenter.isConfigureAvail(CODE_LAST_VISIT_DATE)) {
            lastVisitFromDate.setText(getString(R.string.select_date));
            lastVisitToDate.setText(getString(R.string.select_date));
        }
        if (presenter.isConfigureAvail(CODE_TASK_DUE_DATE)) {
            taskFromDate.setText(getString(R.string.select_date));
            taskToDate.setText(getString(R.string.select_date));
        }

    }

    @OnClick(R.id.filter_btn)
    void filterButtonListener(){

        planFilterBo = new RetailerPlanFilterBo();

        if (presenter.isConfigureAvail(CODE_IS_NOT_VISITED))
            planFilterBo.setIsNotVisited(notVisitedCheckBox.isChecked()?1:0);

        if (presenter.isConfigureAvail(CODE_LAST_VISIT_DATE))
            planFilterBo.setLastVisitDate(new FilterObjectBo(lastVisitFromDate.getText().toString(),
                lastVisitToDate.getText().toString()));
        if (presenter.isConfigureAvail(CODE_TASK_DUE_DATE))
            planFilterBo.setTaskDate(new FilterObjectBo(taskFromDate.getText().toString(),
                taskToDate.getText().toString()));

        presenter.validateFilterObject(planFilterBo);
    }

    private void showDatePicker(String date,String minimumDate,View view) {
        // date picker dialog

        int day;
        int month;
        int year;

        if (!date.isEmpty()) {
            String[] splitDate = date.split("/");

            day = SDUtil.convertToInt(splitDate[0]);
            month = SDUtil.convertToInt(splitDate[1]) - 1;
            year = SDUtil.convertToInt(splitDate[2]);

        }else{
            Calendar c = Calendar.getInstance();
            year = c.get(Calendar.YEAR);
            month = c.get(Calendar.MONTH) -1 ;
            day = c.get(Calendar.DAY_OF_MONTH);
        }

        DatePickerViewDialog picker = new DatePickerViewDialog(context, R.style.SellerDatePickerStyle, this,
                day, month, year, view);

        if (!minimumDate.isEmpty()) {
            boolean isFromDate = false;
            if (view.getId() == R.id.task_due_from_date)
                isFromDate = true;

            picker.compareDate(minimumDate,isFromDate );
        }

        picker.updateDate(year, month, day);

        picker.show();
    }

    private BottomSheetBehavior.BottomSheetCallback bottomSheetCallBack = new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {

            switch (newState) {
                case BottomSheetBehavior.STATE_DRAGGING:
                    break;
                case BottomSheetBehavior.STATE_SETTLING:
                    break;
                case BottomSheetBehavior.STATE_EXPANDED:
                    break;
                case BottomSheetBehavior.STATE_COLLAPSED:
                    break;
                default:
                    break;
                case BottomSheetBehavior.STATE_HIDDEN: {
                    dismiss();
                    break;
                }
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        }
    };

    @Override
    protected void getMessageFromAliens() {

    }

    @Override
    protected void setUpViews() {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            return true;
        }

        return false;
    }

    @Override
    public void onDateSet(View view, String date) {
        ((TextView)view).setText(date);
    }

    @Override
    public void dateValidationError(String error) {
        showMessage(error);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

    }

    @Override
    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

    }

    @Override
    public void showNotVisitedRow() {
        View view = getLayoutInflater().inflate(R.layout.not_visit_check_box_layout, null);
        notVisitedCheckBox = view.findViewById(R.id.not_visited_check_box);

        if (planFilterBo != null)
            notVisitedCheckBox.setChecked(planFilterBo.getIsNotVisited()>0);

        dynamicViewLayout.addView(view);
    }

    @Override
    public void showTaskDueDateRow() {
        View view = getLayoutInflater().inflate(R.layout.date_layout, null);
        taskFromDate = view.findViewById(R.id.task_due_from_date);
        taskToDate = view.findViewById(R.id.task_due_to_date);

        taskFromDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateSelectListener(taskFromDate,"");
            }
        });

        taskToDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (taskFromDate.getText().toString().equalsIgnoreCase(getString(R.string.select_date))){
                    showMessage("Select From Date");
                    return;
                }

                dateSelectListener(taskToDate,taskFromDate.getText().toString());
            }
        });

        ((TextView)view.findViewById(R.id.task_due_date_txt)).setText(getString(R.string.task_due_date));

        if (planFilterBo != null && planFilterBo.getTaskDate() != null){
            taskFromDate.setText(planFilterBo.getTaskDate().getStringOne());
            taskToDate.setText(planFilterBo.getTaskDate().getStringTwo());
        }

        dynamicViewLayout.addView(view);
    }

    @Override
    public void showLastVisitRow() {
        View view = getLayoutInflater().inflate(R.layout.date_layout, null);
        lastVisitFromDate = view.findViewById(R.id.task_due_from_date);
        lastVisitToDate = view.findViewById(R.id.task_due_to_date);

        lastVisitFromDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateSelectListener(lastVisitFromDate,"");
            }
        });

        lastVisitToDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lastVisitFromDate.getText().toString().equalsIgnoreCase(getString(R.string.select_date))){
                    showMessage("Select From Date");
                    return;
                }
                dateSelectListener(lastVisitToDate,lastVisitFromDate.getText().toString());
            }
        });

        if (planFilterBo != null && planFilterBo.getLastVisitDate() != null){
            lastVisitFromDate.setText(planFilterBo.getLastVisitDate().getStringOne());
            lastVisitToDate.setText(planFilterBo.getLastVisitDate().getStringTwo());
        }

        ((TextView)view.findViewById(R.id.task_due_date_txt)).setText(getString(R.string.last_visit_date));

        dynamicViewLayout.addView(view);
    }

    @Override
    public void showAttributeSpinner() {

        ArrayList<AttributeBO> attributeBOList = new ArrayList<>(presenter.getAttributeMapValues().values());

        for (AttributeBO attributeBO : attributeBOList){
            View view = getLayoutInflater().inflate(R.layout.attribute_spinner_layout, null);

            ((TextView)view.findViewById(R.id.spinner_txt)).setText(attributeBO.getAttributeName());

            Spinner attributeSpinner = view.findViewById(R.id.spinner_attribute);

            ArrayAdapter<AttributeBO> attributeAdapter  = new ArrayAdapter<AttributeBO>(context,
                    android.R.layout.simple_spinner_item,
                    new ArrayList<>(attributeBO.getAttributeBOHashMap().values()));
            attributeAdapter.setDropDownViewResource(R.layout.spinner_bluetext_list_item);
            attributeSpinner.setAdapter(attributeAdapter);

            dynamicViewLayout.addView(view);

        }
    }

    @Override
    public void filterValidationSuccess() {
        presenter.getRetailerFilterArray(planFilterBo);
    }

    @Override
    public void clearFilter() {
        EventBus.getDefault().post("CLEAR");
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    @Override
    public void noFilterRecord() {
        EventBus.getDefault().post("NODATA");
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    @Override
    public void filterValidationFailure(String error) {
        showMessage(error);
    }

    @Override
    public void filteredRetailerIds(ArrayList<String> retailerIds) {

        planFilterBo.setRetailerIds(retailerIds);

        EventBus.getDefault().post(planFilterBo);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

}
