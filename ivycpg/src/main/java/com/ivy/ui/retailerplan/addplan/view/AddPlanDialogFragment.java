package com.ivy.ui.retailerplan.addplan.view;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.Group;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.ivy.core.base.view.BaseBottomSheetDialogFragment;
import com.ivy.cpg.view.profile.CommonReasonDialog;
import com.ivy.cpg.view.profile.ProfileActivity;
import com.ivy.cpg.view.retailercontact.RetailerContactAvailBo;
import com.ivy.cpg.view.retailercontact.TimeSlotPickFragment;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.MyDatePickerDialog;
import com.ivy.ui.retailerplan.addplan.AddPlanContract;
import com.ivy.ui.retailerplan.addplan.DateWisePlanBo;
import com.ivy.ui.retailerplan.addplan.di.AddPlanModule;
import com.ivy.ui.retailerplan.addplan.di.DaggerAddPlanComponent;
import com.ivy.ui.retailerplan.addplan.presenter.AddPlanPresenterImpl;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.DeviceUtils;
import com.ivy.utils.StringUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;

import static com.ivy.ui.retailer.RetailerConstants.APPROVED;
import static com.ivy.ui.retailer.RetailerConstants.COMPLETED;
import static com.ivy.ui.retailer.RetailerConstants.PLANNED;
import static com.ivy.utils.DateTimeUtils.DATE_GLOBAL;
import static com.ivy.utils.DateTimeUtils.TIME_HOUR_MINS;

@SuppressLint("ValidFragment")
public class AddPlanDialogFragment extends BaseBottomSheetDialogFragment implements AddPlanContract.AddPlanView,
        CommonReasonDialog.AddNonVisitListener, CommonDialog.PositiveClickListener {

    @BindView(R.id.tv_add)
    TextView addPlan;

    @BindView(R.id.tv_mail)
    TextView sendEmail;

    @BindView(R.id.tv_profile)
    TextView userProfile;

    @BindView(R.id.tv_edit)
    ImageView editPlan;

    @BindView(R.id.tv_save)
    TextView savePlan;

    @BindView(R.id.tv_cancel)
    TextView cancelPlan;

    @BindView(R.id.tv_delete)
    TextView deletePlan;

    @BindView(R.id.tv_outlet_name)
    TextView tvOutletName;

    @BindView(R.id.tv_outlet_address)
    TextView tvOutletAddress;

    @BindView(R.id.tv_last_visit_date)
    TextView tvLastVisitDate;

    @BindView(R.id.tv_last_visit_txt)
    TextView tvLastVisitTxt;

    @BindView(R.id.tv_visit_time)
    TextView tvStartVisitTime;

//    @BindView(R.id.tv_visit_date)
//    TextView tvStartVisitDate;
//
//    @BindView(R.id.tv_visit_end_date)
//    TextView tvVisitEndDate;

    @BindView(R.id.tv_visit_end_time)
    TextView tvVisitEndTime;

    @BindView(R.id.tv_planned_layout_txt)
    TextView tvPlannedLayoutTxt;

    @BindView(R.id.visitElementGroup)
    Group visitElementGroup;

    @BindView(R.id.saveElementGroup)
    Group saveElementGroup;

    @BindView(R.id.planned_slot_grid_layout)
    GridLayout timeSlotGridLayout;

    @BindView(R.id.seperator_view)
    View seperatorView;

    @BindView(R.id.tv_date_selection_txt)
    TextView tvDateSelection;

    @BindView(R.id.btn_date_picker)
    Button btnDateSelection;

    private BottomSheetBehavior bottomSheetBehavior;

    private Context context;

    private RetailerMasterBO retailerMasterBO;

    private DateWisePlanBo dateWisePlanBo;

    private TimeSlotPickFragment timeSlotPickFragment;

    private ArrayList<DateWisePlanBo> planList;

    private MyDatePickerDialog datePickerDialog;
    private int mYear;
    private int mMonth;
    private int mDay;

    @Inject
    AddPlanPresenterImpl<AddPlanContract.AddPlanView> addPlanPresenter;

    public AddPlanDialogFragment(String selectedDate, RetailerMasterBO retailerMaster,
                                 DateWisePlanBo dateWisePlanBo, ArrayList<DateWisePlanBo> planList) {
        retailerMasterBO = retailerMaster;
        this.dateWisePlanBo = dateWisePlanBo;
        this.planList = planList;
        this.selectedDate = selectedDate;

        startTime = DateTimeUtils.now(TIME_HOUR_MINS)+":00";

        int endTimeDifference = Integer.parseInt(DateTimeUtils.now(TIME_HOUR_MINS))+1;
        endtime = endTimeDifference+":00";
    }

    public AddPlanDialogFragment(String selectedDate, RetailerMasterBO retailerMaster,
                                 DateWisePlanBo dateWisePlanBo, ArrayList<DateWisePlanBo> planList, String startTime){
        retailerMasterBO = retailerMaster;
        this.dateWisePlanBo = dateWisePlanBo;
        this.planList = planList;
        this.selectedDate = selectedDate;

        this.startTime = startTime+":00";
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, SDUtil.convertToInt(startTime));
        calendar.add(Calendar.HOUR, 1);
        this.endtime = String.format(Locale.ENGLISH,"%02d",calendar.get(Calendar.HOUR_OF_DAY))+":00";

        Commons.print("StartTime:"+this.startTime);
        Commons.print("EndTime:"+this.endtime);
    }

    private String selectedDate="",startTime="",endtime="";

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    protected int setContentViewLayout() {
        return R.layout.retailer_plan_info_layout;
    }

    @Override
    public void initVariables(Dialog dialog,View view) {

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) view.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();

        addPlan.setOnClickListener(addToPlanListener);
        editPlan.setOnClickListener(addToPlanListener);

        if (behavior != null && behavior instanceof BottomSheetBehavior) {

            bottomSheetBehavior = ((BottomSheetBehavior) behavior);

            bottomSheetBehavior.setHideable(false);

            bottomSheetBehavior.setPeekHeight(DeviceUtils.getDisplayMetrics(context).heightPixels);

            ((BottomSheetBehavior) behavior).setState(BottomSheetBehavior.STATE_EXPANDED);

            ((BottomSheetBehavior) behavior).setBottomSheetCallback(bottomSheetCallBack);
        }


        if (selectedDate == null || selectedDate.isEmpty())
            selectedDate = DateTimeUtils.now(DATE_GLOBAL);

        if (dateWisePlanBo != null && !dateWisePlanBo.getStartTime().isEmpty())
            startTime = dateWisePlanBo.getStartTime();

        if (dateWisePlanBo != null && !dateWisePlanBo.getEndTime().isEmpty())
            endtime = dateWisePlanBo.getEndTime();

        setPlanWindowValues();
    }

    @Override
    public void initializeDi(){
        DaggerAddPlanComponent.builder()
                .ivyAppComponent(((BusinessModel) Objects.requireNonNull((FragmentActivity)context).getApplication()).getComponent())
                .addPlanModule(new AddPlanModule(this, context))
                .build()
                .inject(AddPlanDialogFragment.this);

        setBasePresenter(addPlanPresenter);
    }

    @Override
    protected void getMessageFromAliens() {

    }

    @Override
    protected void setUpViews() {

    }

    private BottomSheetBehavior.BottomSheetCallback bottomSheetCallBack = new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {

            switch (newState) {
                case BottomSheetBehavior.STATE_DRAGGING: {
                    break;
                }
                case BottomSheetBehavior.STATE_SETTLING: {
                    break;
                }
                case BottomSheetBehavior.STATE_EXPANDED: {

                    if (savePlan.getVisibility() == View.VISIBLE) {
                        tvStartVisitTime.setOnClickListener(startVisitTimeListener);
                        tvVisitEndTime.setOnClickListener(startVisitTimeListener);
                    }else {
                        tvStartVisitTime.setOnClickListener(null);
                        tvVisitEndTime.setOnClickListener(null);
                    }

                    break;
                }
                case BottomSheetBehavior.STATE_COLLAPSED:
                    savePlan.setVisibility(View.GONE);

                    if (dateWisePlanBo == null || dateWisePlanBo.getVisitStatus() == null)
                        visitElementGroup.setVisibility(View.GONE);

                    setViewBackground(null);

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

    private View.OnClickListener startVisitTimeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            RetailerContactAvailBo retailerContactAvailBo =  new RetailerContactAvailBo();
            retailerContactAvailBo.setDay("Monday");
            retailerContactAvailBo.setFrom(tvStartVisitTime.getText().toString());
            retailerContactAvailBo.setTo(tvVisitEndTime.getText().toString());

            FragmentTransaction ft = ((FragmentActivity) context).getSupportFragmentManager().beginTransaction();
            timeSlotPickFragment = new TimeSlotPickFragment(false,true,true
                    ,"", "Visit From","Visit Till","Retailer Visit Time",retailerContactAvailBo);
            timeSlotPickFragment.setCancelable(true);
            timeSlotPickFragment.show(ft, "TimeSlotFragment");
        }
    };

    @OnClick(R.id.tv_profile)
    void viewProfile(){
//        presenter.setRetailerMasterBo(retailerMasterBO);
        Intent i = new Intent(context, ProfileActivity.class);
        i.putExtra("From", "RetailerView");
        i.putExtra("RetailerViewDate",selectedDate);

        int dateCount = DateTimeUtils.getDateCount(selectedDate,DateTimeUtils.now(DATE_GLOBAL),"yyyy/MM/dd");
        if (dateCount > 0) {
            i.putExtra("HideStartVisit", true);
            i.putExtra("HideCancelVisit", true);
        }else if (dateCount < 0){
            i.putExtra("HideStartVisit", true);
            if (dateWisePlanBo != null && dateWisePlanBo.getVisitStatus().equalsIgnoreCase(PLANNED) && dateWisePlanBo.getPlanStatus().equalsIgnoreCase(APPROVED))
                i.putExtra("HideCancelVisit", false);
            else
                i.putExtra("HideCancelVisit", true);
        }else{
            if (dateWisePlanBo != null) {
                if ((dateWisePlanBo.getVisitStatus().equalsIgnoreCase(PLANNED) && dateWisePlanBo.getPlanStatus().equalsIgnoreCase(APPROVED))
                        || dateWisePlanBo.getVisitStatus().equalsIgnoreCase(COMPLETED) )
                    i.putExtra("HideStartVisit", false);
                else
                    i.putExtra("HideStartVisit", true);

                if (dateWisePlanBo.getVisitStatus().equalsIgnoreCase(PLANNED) && dateWisePlanBo.getPlanStatus().equalsIgnoreCase(APPROVED))
                    i.putExtra("HideCancelVisit", false);
                else
                    i.putExtra("HideCancelVisit", true);
            }else {
                i.putExtra("HideStartVisit", true);
                i.putExtra("HideCancelVisit", true);
            }
        }

        startActivity(i);
    }

    @OnClick(R.id.tv_save)
    void savePlan(){

        String startTime = tvStartVisitTime.getText().toString();
        String endTime = tvVisitEndTime.getText().toString();

        if (validate(startTime,endTime)) {

            if (dateWisePlanBo != null && dateWisePlanBo.getVisitStatus().equalsIgnoreCase(PLANNED)) {
                addPlanPresenter.updatePlan( startTime, endTime, dateWisePlanBo);
            } else {
                addPlanPresenter.addNewPlan(selectedDate, startTime, endTime, retailerMasterBO);
            }
        }
    }

    private boolean validate(String startTime, String endtime){

        for (DateWisePlanBo planBo : planList) {
            if (!(planBo.getEntityId()+"").equalsIgnoreCase(retailerMasterBO.getRetailerID()) ){
                if (DateTimeUtils.isBetweenTime(startTime, endtime, planBo.getStartTime(), true)) {
                    showMessage(getString(R.string.time_slot_already_selected));
                    return false;
                } else if (DateTimeUtils.isBetweenTime(startTime, endtime, planBo.getEndTime(), false)) {
                    showMessage(getString(R.string.time_slot_already_selected));
                    return false;
                }
            }
        }

        return true;

    }

    @OnClick(R.id.tv_mail)
    void sendEmail(){

        if (retailerMasterBO.getEmail() != null && StringUtils.isValidEmail(retailerMasterBO.getEmail())) {

            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto", retailerMasterBO.getEmail(), null));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Message To Retailer");
            emailIntent.putExtra(Intent.EXTRA_TEXT, "Hi "+retailerMasterBO.getRetailerName());
            startActivity(Intent.createChooser(emailIntent, "Send email..."));
        }else
            showMessage("No Valid Email Found");

    }

    @OnClick(R.id.tv_cancel)
    void cancelPlan(){

        CommonReasonDialog comReasonDialog = new CommonReasonDialog(context, "nonVisit");
        comReasonDialog.setNonvisitListener(this);
        comReasonDialog.getFromScreenParam("RetailerView",selectedDate);
        comReasonDialog.show();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = comReasonDialog.getWindow();
        lp.copyFrom(window != null ? window.getAttributes() : null);
        lp.width = DeviceUtils.getDisplayMetrics(context).widthPixels - 100;
        if (window != null) {
            window.setAttributes(lp);
        }
    }

    @OnClick(R.id.tv_cancel_edit)
    void cancelEditView(){

        setPlanWindowValues();
    }

    @OnClick(R.id.tv_delete)
    void deletePlan(){

        showAlert("Delete Plan", "Are You Sure Want To Delete ?", this, true);
    }

    @OnClick(R.id.btn_date_picker)
    void datePickerDialog() {
        if (datePickerDialog == null)
            createDatePickerDialog();

        datePickerDialog.show();
    }

    private View.OnClickListener addToPlanListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (addPlan.getVisibility() == View.VISIBLE){
                visitElementGroup.setVisibility(View.VISIBLE);
            }

            setViewBackground(ContextCompat.getDrawable(context,R.drawable.edittext_full_border));

            tvStartVisitTime.setOnClickListener(startVisitTimeListener);
            tvVisitEndTime.setOnClickListener(startVisitTimeListener);

            saveElementGroup.setVisibility(View.VISIBLE);
            hideShowViews(8);

            seperatorView.setVisibility(View.VISIBLE);

            addPlan.setVisibility(View.GONE);
            editPlan.setVisibility(View.GONE);

            savePlan.setVisibility(View.VISIBLE);

//            tvStartVisitDate.setText(selectedDate);
//            tvVisitEndDate.setText(selectedDate);

            tvStartVisitTime.setText(startTime);
            tvVisitEndTime.setText(endtime);

            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED)
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

            if (planList != null && !planList.isEmpty()){

                if (planList.size() == 1){
                    if (String.valueOf(planList.get(0).getEntityId()).equalsIgnoreCase(retailerMasterBO.getRetailerID()))
                        return;
                }

                if (DeviceUtils.isTabletDevice(context))
                    timeSlotGridLayout.setColumnCount(5);
                else
                    timeSlotGridLayout.setColumnCount(3);

                timeSlotGridLayout.removeAllViews();
                timeSlotGridLayout.setVisibility(View.VISIBLE);
                tvPlannedLayoutTxt.setVisibility(View.VISIBLE);

                for (DateWisePlanBo planBo : planList){

                    if (String.valueOf(planBo.getEntityId()).equalsIgnoreCase(retailerMasterBO.getRetailerID()))
                        continue;

                    String timeSlot = planBo.getStartTime()+" - "+planBo.getEndTime();

                    View view = getLayoutInflater().inflate(R.layout.time_slot_textview_layout, null);

                    ((TextView)view.findViewById(R.id.time_slot_textview)).setText(timeSlot);

                    timeSlotGridLayout.addView(view);
                }
            }else{
                tvPlannedLayoutTxt.setVisibility(View.GONE);
                timeSlotGridLayout.setVisibility(View.GONE);
            }
        }
    };

    private void setViewBackground(Drawable viewBackground){
        tvStartVisitTime.setBackground(viewBackground);
        tvVisitEndTime.setBackground(viewBackground);
    }

    private void setPlanWindowValues(){

        setViewBackground(null);
        userProfile.setVisibility(View.VISIBLE);
        sendEmail.setVisibility(View.VISIBLE);
        tvPlannedLayoutTxt.setVisibility(View.GONE);
        timeSlotGridLayout.setVisibility(View.GONE);
        seperatorView.setVisibility(View.GONE);

        saveElementGroup.setVisibility(View.GONE);

        tvStartVisitTime.setOnClickListener(null);
        tvVisitEndTime.setOnClickListener(null);

        tvOutletName.setText(retailerMasterBO.getRetailerName());
        tvOutletAddress.setText(retailerMasterBO.getAddress1());

        saveElementGroup.setVisibility(View.GONE);

        if (dateWisePlanBo != null
                && dateWisePlanBo.getVisitStatus().equalsIgnoreCase(PLANNED)
                && (retailerMasterBO != null && !"Y".equals(retailerMasterBO.getIsVisited()) )) {

            visitElementGroup.setVisibility(View.VISIBLE);

            tvLastVisitDate.setText(retailerMasterBO.getLastVisitDate());

            addPlan.setVisibility(View.GONE);

            if (DateTimeUtils.getDateCount(selectedDate,DateTimeUtils.now(DATE_GLOBAL),"yyyy/MM/dd") <= 0) {

                if (dateWisePlanBo.getVisitStatus() == null) {
                    cancelPlan.setVisibility(View.GONE);
                    deletePlan.setVisibility(View.GONE);
                } else if (dateWisePlanBo.isServerData() && dateWisePlanBo.getVisitStatus().equalsIgnoreCase(PLANNED)) {
                    cancelPlan.setVisibility(View.VISIBLE);
                    deletePlan.setVisibility(View.GONE);
                } else if (!dateWisePlanBo.isServerData() && dateWisePlanBo.getVisitStatus().equalsIgnoreCase(PLANNED)) {
                    cancelPlan.setVisibility(View.GONE);
                    deletePlan.setVisibility(View.VISIBLE);
                }

                if (dateWisePlanBo.getVisitStatus().equalsIgnoreCase(PLANNED)) {
                    editPlan.setVisibility(View.VISIBLE);
                } else {
                    editPlan.setVisibility(View.GONE);
                }
            }

//            tvStartVisitDate.setText(selectedDate);
//            tvVisitEndDate.setText(selectedDate);

            tvStartVisitTime.setText(startTime);
            tvVisitEndTime.setText(endtime);

        }else {
            visitElementGroup.setVisibility(View.GONE);
            editPlan.setVisibility(View.GONE);

            if (DateTimeUtils.getDateCount(selectedDate,DateTimeUtils.now(DATE_GLOBAL),"yyyy/MM/dd") <= 0){
                    if (
                            (dateWisePlanBo != null && (dateWisePlanBo.getVisitStatus().equalsIgnoreCase(COMPLETED)
                        || dateWisePlanBo.getVisitStatus().equalsIgnoreCase(PLANNED)
                        || dateWisePlanBo.getVisitStatus().equalsIgnoreCase("CANCELLED"))
                            )
                        || (retailerMasterBO != null && "Y".equals(retailerMasterBO.getIsVisited()) )){

                        addPlan.setVisibility(View.GONE);
                    }else
                        addPlan.setVisibility(View.VISIBLE);
            }

            final Calendar c = Calendar.getInstance();
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);
            btnDateSelection.setText(DateTimeUtils.convertFromServerDateToRequestedFormat(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL), ConfigurationMasterHelper.outDateFormat));
        }

    }

    private void createDatePickerDialog() {
        datePickerDialog = new MyDatePickerDialog(
                context, R.style.DatePickerDialogStyle,
                (view, year, monthOfYear, dayOfMonth) -> {
                    mYear = year;
                    mMonth = monthOfYear;
                    mDay = dayOfMonth;
                    Calendar selectedDate = new GregorianCalendar(
                            year, monthOfYear, dayOfMonth);

                    String selectedDateStr = DateTimeUtils
                            .convertDateObjectToRequestedFormat(
                                    selectedDate.getTime(),
                                    ConfigurationMasterHelper.outDateFormat);

                    btnDateSelection.setText(selectedDateStr);
                    Calendar mCurrentCalendar = Calendar
                            .getInstance();
                    if (DateTimeUtils.getDateCount(selectedDateStr, DateTimeUtils.now(DATE_GLOBAL), "yyyy/MM/dd") <= 0) {

                        showMessage(getString(R.string.select_future_date));

                        btnDateSelection.setText(DateTimeUtils.convertFromServerDateToRequestedFormat(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL), ConfigurationMasterHelper.outDateFormat));

                        mYear = mCurrentCalendar.get(Calendar.YEAR);
                        mMonth = mCurrentCalendar.get(Calendar.MONTH);
                        mDay = mCurrentCalendar.get(Calendar.DAY_OF_MONTH);

                    }
                    this.selectedDate = selectedDateStr;
                }, mYear, mMonth, mDay);
        datePickerDialog.setPermanentTitle(getString(R.string.choose_date));
    }

    @Subscribe
    public void onMessageEvent(RetailerContactAvailBo contactAvailBo) {

        tvStartVisitTime.setText(contactAvailBo.getFrom());
        tvVisitEndTime.setText(contactAvailBo.getTo());

        if (timeSlotPickFragment != null)
            timeSlotPickFragment.dismiss();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);

    }

    @Override
    public void onStop() {
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void showUpdatedSuccessfullyMessage() {

    }

    @Override
    public void showUpdateFailureMessage() {

    }


    @Override
    public void updateDatePlan(DateWisePlanBo planBo) {
        EventBus.getDefault().post(planBo);
        dismiss();
    }


    @Override
    public void addReatailerReason() {
        addPlanPresenter.cancelPlan(dateWisePlanBo,retailerMasterBO);
        showMessage(getString(R.string.saved_successfully));
    }

    @Override
    public void onDismiss() {
        dismiss();
    }

    @Override
    public void onPositiveButtonClick() {
        addPlanPresenter.deletePlan(dateWisePlanBo,retailerMasterBO);
    }

    private void hideShowViews(int isShow){

        addPlan.setVisibility(isShow);
        editPlan.setVisibility(isShow);
        deletePlan.setVisibility(isShow);
        cancelPlan.setVisibility(isShow);
        sendEmail.setVisibility(isShow);
        userProfile.setVisibility(isShow);
    }
}
