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
import android.widget.AdapterView;
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
import java.util.HashMap;
import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;

import static com.ivy.ui.retailer.RetailerConstants.CODE_IS_NOT_VISITED;
import static com.ivy.ui.retailer.RetailerConstants.CODE_LAST_VISIT_DATE;
import static com.ivy.ui.retailer.RetailerConstants.CODE_TASK_DUE_DATE;

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

    private HashMap<String,Spinner> attributeSpinner = new HashMap<>();

    private HashMap<String,AttributeBO> selectedSpinnerIds = new HashMap<>();

    private ArrayList<String> filteredAttributeIds = new ArrayList<>();

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

            bottomSheetBehavior.setHideable(false);

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
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    @OnClick(R.id.clear_btn)
    void clearButtonListener(){

        if (planFilterBo != null)
            planFilterBo = new RetailerPlanFilterBo();

        dynamicViewLayout.removeAllViews();

        presenter.prepareScreenData();
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

        if (!selectedSpinnerIds.isEmpty()) {

            ArrayList<AttributeBO> ids = new ArrayList<>(selectedSpinnerIds.values());

            filteredAttributeIds.clear();
            for (AttributeBO bo : ids){
                if (bo.isAttributeSelected()) {
                    String[] spliArr = bo.getAttributeName().split("##");
                    String[] spliArr1 = spliArr[1].split("__");
                    filteredAttributeIds.add(spliArr1[0]);
                }
            }

            planFilterBo.setFilterAttributeIds(filteredAttributeIds);

            planFilterBo.setFilterAttributeIdMap(selectedSpinnerIds);
        }

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
                    bottomSheetBehavior.setHideable(false);
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
            bottomSheetBehavior.setHideable(true);
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

        ArrayList<AttributeBO> attributeBOList = presenter.getAttributeListValues();

        for (AttributeBO attributeBO : attributeBOList){

            ArrayList<AttributeBO> attributeChildList = new ArrayList<>(presenter.getAttributeChildLst(attributeBO.getAttributeId()+""));

            View view = getLayoutInflater().inflate(R.layout.attribute_spinner_layout, null);

            ((TextView)view.findViewById(R.id.spinner_txt)).setText(attributeBO.getAttributeName());

            Spinner attributeSpinner = view.findViewById(R.id.spinner_attribute);

            attributeSpinner.setTag(attributeBO.getAttributeName()+"##"+attributeBO.getAttributeId()+"__"+1);

            setSpinnerAdapter(attributeSpinner,attributeChildList,false);

            attributeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position != 0){

                        AttributeBO attributeBO1 = (AttributeBO)parent.getSelectedItem();

                        String[] splitTagName = parent.getTag().toString().split("__");
                        updateAttributeArray(parent.getTag().toString(), attributeBO1.getAttributeId());
                        resetParentValues((Spinner) parent);

                        ArrayList<AttributeBO> attributeChildList = presenter.getAttributeChildLst(attributeBO1.getAttributeId()+"");

                        if (attributeChildList == null || attributeChildList.isEmpty()){
                            attributeChildList = new ArrayList<>();
                        }

                        Spinner childSpinner = RetailerPlanFilterFragment.this.attributeSpinner.get(splitTagName[0]+"__"+2);

                        if (childSpinner == null)
                            return;

                        setSpinnerAdapter(childSpinner,attributeChildList,true);
                    }else {
                        updateAttributeArray(parent.getTag().toString(), -1);
                        resetSpinnerAdapter((Spinner) parent);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            if (attributeBO.getLevelCount() > 2){
                for (int i = 2 ; i < attributeBO.getLevelCount(); i++){
                    createChildSpinner(view,attributeBO.getAttributeName()+"##"+attributeBO.getAttributeId()+"__"+i);
                }
            }

            dynamicViewLayout.addView(view);

        }
    }

    private void createChildSpinner(final View baseView, String tagName){

        View view = getLayoutInflater().inflate(R.layout.attribute_spinner_layout, null);

        ((TextView)view.findViewById(R.id.spinner_txt)).setVisibility(View.GONE);

        Spinner attributeSpinner = view.findViewById(R.id.spinner_attribute);

        attributeSpinner.setTag(tagName);

        setSpinnerAdapter(attributeSpinner,new ArrayList<>(),false);

        this.attributeSpinner.put(tagName,attributeSpinner);

        attributeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0){

                    AttributeBO attributeBO1 = (AttributeBO)parent.getSelectedItem();

                    String[] splitTagName = parent.getTag().toString().split("__");
                    updateAttributeArray(parent.getTag().toString(), attributeBO1.getAttributeId());
                    resetParentValues((Spinner) parent);

                    ArrayList<AttributeBO> attributeChildList = presenter.getAttributeChildLst(attributeBO1.getAttributeId()+"");

                    if (attributeChildList == null || attributeChildList.isEmpty()){
                        attributeChildList = new ArrayList<>();
                    }

                    String tagName = splitTagName[0]+"__"+(Integer.parseInt(splitTagName[1])+1);

                    Spinner childSpinner = RetailerPlanFilterFragment.this.attributeSpinner.get(tagName);

                    if (childSpinner == null)
                        return;

                    setSpinnerAdapter(childSpinner,attributeChildList,true);
                }else{
                    updateAttributeArray(parent.getTag().toString(), -1);
                    resetSpinnerAdapter((Spinner) parent);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ((LinearLayout)baseView).addView(view);
    }

    private void resetSpinnerAdapter(Spinner spinner){
        String[] splitTagName = spinner.getTag().toString().split("__");

        String tagName = splitTagName[0]+"__"+(Integer.parseInt(splitTagName[1])+1);

        Spinner childSpinner = attributeSpinner.get(tagName);

        if (childSpinner != null) {
            updateAttributeArray(tagName, -1);
            setSpinnerAdapter(childSpinner, new ArrayList<>(), true);
        }
    }

    private void setSpinnerAdapter(Spinner attributeSpinner, ArrayList<AttributeBO> list,boolean isUpdate){
        AttributeBO attributeBO = new AttributeBO(-1,"Select");

        ArrayList<AttributeBO> spinnerList = new ArrayList<>();
        spinnerList.add(attributeBO);
        spinnerList.addAll(list);

        ArrayAdapter<AttributeBO> attributeAdapter  = new ArrayAdapter<AttributeBO>(context,
                android.R.layout.simple_spinner_item,
                spinnerList);
        attributeAdapter.setDropDownViewResource(R.layout.spinner_bluetext_list_item);
        attributeSpinner.setAdapter(attributeAdapter);

        if (planFilterBo != null
                && planFilterBo.getFilterAttributeIdMap() != null
                && planFilterBo.getFilterAttributeIdMap().get(attributeSpinner.getTag().toString())!= null
                && list != null && !list.isEmpty()) {

            int pos =0;
            AttributeBO attributeBO1 = planFilterBo.getFilterAttributeIdMap().get(attributeSpinner.getTag().toString());
            for (AttributeBO aId : list) {

                if (aId.getAttributeId() == attributeBO1.getAttributeId()) {
                    attributeSpinner.setSelected(true);
                    attributeSpinner.setSelection(pos+1);
                    break;
                }
                pos++;
            }
        }

        if (isUpdate)
            resetSpinnerAdapter(attributeSpinner);
    }

    private void resetParentValues(Spinner spinner){
        String[] splitTagName = spinner.getTag().toString().split("__");

        String tagName = splitTagName[0]+"__"+(Integer.parseInt(splitTagName[1])-1);

        Spinner parentSpinner = attributeSpinner.get(tagName);

        if (parentSpinner != null
                && selectedSpinnerIds != null
                && selectedSpinnerIds.get(tagName) != null) {
            AttributeBO attributeBO = selectedSpinnerIds.get(tagName);
            attributeBO.setAttributeSelected(false);

            resetParentValues(parentSpinner);
        }
    }

    private void updateAttributeArray(String attribute, int id){

        if (selectedSpinnerIds.get(attribute) != null && id  == -1){
            selectedSpinnerIds.remove(attribute);
        }else if (id  != -1) {
            AttributeBO attributeBO = new AttributeBO();
            attributeBO.setAttributeId(id);
            attributeBO.setAttributeName(attribute);
            attributeBO.setAttributeSelected(true);

            selectedSpinnerIds.put(attribute,attributeBO);
        }
    }

    @Override
    public void filterValidationSuccess() {

        if (planFilterBo.getLastVisitDate() != null
                && (planFilterBo.getLastVisitDate().getStringOne().equalsIgnoreCase(getString(R.string.select_date)))){
            planFilterBo.setLastVisitDate(null);
        }
        if(planFilterBo.getTaskDate() != null
                && (planFilterBo.getTaskDate().getStringOne().equalsIgnoreCase(getString(R.string.select_date)))){
            planFilterBo.setTaskDate(null);
        }

        presenter.getRetailerFilterArray(planFilterBo);
    }

    @Override
    public void clearFilter() {
        EventBus.getDefault().post("CLEAR");
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    @Override
    public void noFilterRecord() {
        EventBus.getDefault().post("NODATA");
        bottomSheetBehavior.setHideable(true);
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
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

}
