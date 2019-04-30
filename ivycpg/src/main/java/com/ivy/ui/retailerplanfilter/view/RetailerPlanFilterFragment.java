package com.ivy.ui.retailerplanfilter.view;

import android.app.Dialog;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

import com.ivy.core.base.view.BaseBottomSheetDialogFragment;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.ui.retailerplanfilter.FilterObjectBo;
import com.ivy.ui.retailerplanfilter.RetailerPlanFilterBo;
import com.ivy.ui.retailerplanfilter.RetailerPlanFilterContract;
import com.ivy.ui.retailerplanfilter.di.DaggerRetailerPlanFilterComponent;
import com.ivy.ui.retailerplanfilter.di.RetailerPlanFilterModule;
import com.ivy.ui.retailerplanfilter.presenter.RetailerPlanFilterPresenterImpl;
import com.ivy.utils.DeviceUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.Calendar;
import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;

public class RetailerPlanFilterFragment extends BaseBottomSheetDialogFragment
        implements RetailerPlanFilterContract.RetailerPlanFilterView,DatePickerViewDialog.DateSelectListener {

    private BottomSheetBehavior bottomSheetBehavior;

    @BindView(R.id.not_visited_check_box)
    AppCompatCheckBox notVisitedCheckBox;

    @BindView(R.id.task_due_from_date)
    TextView taskFromDate;

    @BindView(R.id.task_due_to_date)
    TextView taskToDate;

    @BindView(R.id.last_visit_from_date)
    TextView lastVisitFromDate;

    @BindView(R.id.last_visit_to_date)
    TextView lastVisitToDate;

    @Inject
    RetailerPlanFilterPresenterImpl<RetailerPlanFilterContract.RetailerPlanFilterView> presenter;

    private DatePickerViewDialog picker;

    private String date = "2019/04/22";

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

            bottomSheetBehavior.setPeekHeight(DeviceUtils.getDisplayMetrics(context).heightPixels);

            ((BottomSheetBehavior) behavior).setState(BottomSheetBehavior.STATE_EXPANDED);

            ((BottomSheetBehavior) behavior).setBottomSheetCallback(bottomSheetCallBack);
        }
    }

    @OnClick({R.id.task_due_from_date,R.id.task_due_to_date,R.id.last_visit_from_date,R.id.last_visit_to_date})
    void dateSelectListener(TextView textView){

        String date = textView.getText().toString().contains("/")?textView.getText().toString():"";

        showDatePicker(date,textView);
    }

    @OnClick(R.id.close)
    void closeIconClick(){
        dismiss();
    }

    @OnClick(R.id.clear_btn)
    void clearButtonListener(){
        dismiss();
    }

    @OnClick(R.id.filter_btn)
    void filterButtonListener(){

        RetailerPlanFilterBo planFilterBo = new RetailerPlanFilterBo();
        planFilterBo.setNotVisited(notVisitedCheckBox.isChecked());
        planFilterBo.setLastVisitDate(new FilterObjectBo(lastVisitFromDate.getText().toString(),
                lastVisitToDate.getText().toString()));
        planFilterBo.setTaskDate(new FilterObjectBo(taskFromDate.getText().toString(),
                taskToDate.getText().toString()));

        presenter.validateFilterObject(planFilterBo);
    }

    private void showDatePicker(String date,View view) {
        // date picker dialog

        int day;
        int month;
        int year;

        if (!date.isEmpty()) {
            String[] splitDate = date.split("/");

            day = SDUtil.convertToInt(splitDate[2]);
            month = SDUtil.convertToInt(splitDate[1]);
            year = SDUtil.convertToInt(splitDate[0]);

        }else{
            Calendar c = Calendar.getInstance();
            year = c.get(Calendar.YEAR);
            month = c.get(Calendar.MONTH);
            day = c.get(Calendar.DAY_OF_MONTH);
        }

        picker = new DatePickerViewDialog(context, R.style.SellerDatePickerStyle, this,
                day, month, year,view);
        picker.updateDate(year, month - 1, day);

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
            dismiss();
            return true;
        }

        return false;
    }

    @Override
    public void onDateSet(View view, String date) {

        ((TextView)view).setText(date);

    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

    }

    @Override
    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

    }


    @Override
    public void showNotVisitedRow() {

    }

    @Override
    public void showTaskDueDateRow() {

    }

    @Override
    public void showLastVisitRow() {

    }

    @Override
    public void filterValidationSuccess(RetailerPlanFilterBo planFilterBo) {
        EventBus.getDefault().post(planFilterBo);
        dismiss();
    }

    @Override
    public void filterValidationFailure() {
    }
}
