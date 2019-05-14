package com.ivy.ui.retailerplan.addplan.view;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.constraint.Group;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.core.base.view.BaseBottomSheetDialogFragment;
import com.ivy.cpg.view.profile.CommonReasonDialog;
import com.ivy.cpg.view.profile.ProfileActivity;
import com.ivy.cpg.view.retailercontact.RetailerContactAvailBo;
import com.ivy.cpg.view.retailercontact.TimeSlotPickFragment;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.model.BusinessModel;
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
import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.ivy.ui.retailer.RetailerConstants.COMPLETED;
import static com.ivy.ui.retailer.RetailerConstants.PLANNED;
import static com.ivy.utils.DateTimeUtils.DATE_GLOBAL;
import static com.ivy.utils.DateTimeUtils.TIME_HOUR_MINS;

@SuppressLint("ValidFragment")
public class AddPlanDialogFragment extends BaseBottomSheetDialogFragment implements AddPlanContract.AddPlanView,
        CommonReasonDialog.AddNonVisitListener {

    @BindView(R.id.tv_add)
    TextView addPlan;

    @BindView(R.id.tv_edit)
    TextView editPlan;

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

    @BindView(R.id.tv_visit_time)
    TextView tvStartVisitTime;

    @BindView(R.id.tv_visit_date)
    TextView tvStartVisitDate;

    @BindView(R.id.tv_visit_end_date)
    TextView tvVisitEndDate;

    @BindView(R.id.tv_visit_end_time)
    TextView tvVisitEndTime;

    @BindView(R.id.tv_planned_layout_txt)
    TextView tvPlannedLayoutTxt;

    @BindView(R.id.visitElementGroup)
    Group visitElementGroup;

    @BindView(R.id.planned_slot_grid_layout)
    GridLayout timeSlotGridLayout;

    private BottomSheetBehavior bottomSheetBehavior;

    private Context context;

    private RetailerMasterBO retailerMasterBO;

    private DateWisePlanBo dateWisePlanBo;

    private TimeSlotPickFragment timeSlotPickFragment;

    private ArrayList<DateWisePlanBo> planList;

    @Inject
    AddPlanPresenterImpl<AddPlanContract.AddPlanView> addPlanPresenter;

    public AddPlanDialogFragment(String selectedDate,RetailerMasterBO retailerMaster,
                                 DateWisePlanBo dateWisePlanBo, ArrayList<DateWisePlanBo> planList){
        retailerMasterBO = retailerMaster;
        this.dateWisePlanBo = dateWisePlanBo;
        this.planList = planList;
        this.selectedDate = selectedDate;
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

        startTime = DateTimeUtils.now(TIME_HOUR_MINS)+":00";
        endtime = DateTimeUtils.now(TIME_HOUR_MINS)+":00";

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
        i.putExtra("From", "RetailerMap");
        i.putExtra("RetailerViewDate",selectedDate);

        int dateCount = DateTimeUtils.getDateCount(selectedDate,DateTimeUtils.now(DATE_GLOBAL),"yyyy/MM/dd");
        if (dateCount < 0) {
            i.putExtra("HideStartVisit", true);
            i.putExtra("HideCancelVisit", true);
        }else if (dateCount > 0){
            i.putExtra("HideStartVisit", true);
            if (dateWisePlanBo != null && dateWisePlanBo.getVisitStatus().equalsIgnoreCase(PLANNED))
                i.putExtra("HideCancelVisit", false);
            else
                i.putExtra("HideCancelVisit", true);
        }else{
            if (dateWisePlanBo != null) {
                if (dateWisePlanBo.getVisitStatus().equalsIgnoreCase(PLANNED) || dateWisePlanBo.getVisitStatus().equalsIgnoreCase(COMPLETED) )
                    i.putExtra("HideStartVisit", false);
                else
                    i.putExtra("HideStartVisit", true);

                if (dateWisePlanBo.getVisitStatus().equalsIgnoreCase(PLANNED))
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
        comReasonDialog.show();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = comReasonDialog.getWindow();
        lp.copyFrom(window != null ? window.getAttributes() : null);
        lp.width = DeviceUtils.getDisplayMetrics(context).widthPixels - 100;
        lp.height = (DeviceUtils.getDisplayMetrics(context).heightPixels / 2);//WindowManager.LayoutParams.WRAP_CONTENT;
        if (window != null) {
            window.setAttributes(lp);
        }
    }

    @OnClick(R.id.tv_delete)
    void deletePlan(){
        addPlanPresenter.deletePlan(dateWisePlanBo);
    }

    private View.OnClickListener addToPlanListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (addPlan.getVisibility() == View.VISIBLE){

                visitElementGroup.setVisibility(View.VISIBLE);

                setViewBackground(ContextCompat.getDrawable(context,R.drawable.edittext_bottom_border));

            }else{

                setViewBackground(ContextCompat.getDrawable(context,R.drawable.edittext_bottom_border));

                tvStartVisitTime.setOnClickListener(startVisitTimeListener);
                tvVisitEndTime.setOnClickListener(startVisitTimeListener);
            }

            addPlan.setVisibility(View.GONE);
            editPlan.setVisibility(View.GONE);

            savePlan.setVisibility(View.VISIBLE);

            tvStartVisitDate.setText(selectedDate);
            tvVisitEndDate.setText(selectedDate);

            tvStartVisitTime.setText(startTime);
            tvVisitEndTime.setText(endtime);

            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED)
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

            if (planList != null && !planList.isEmpty()){

                tvPlannedLayoutTxt.setVisibility(View.VISIBLE);

                for (DateWisePlanBo planBo : planList){

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

        tvOutletName.setText(retailerMasterBO.getRetailerName());
        tvOutletAddress.setText(retailerMasterBO.getAddress1());

        if (dateWisePlanBo != null && dateWisePlanBo.getVisitStatus().equalsIgnoreCase(PLANNED)) {

            visitElementGroup.setVisibility(View.VISIBLE);

            tvLastVisitDate.setText(retailerMasterBO.getLastVisitDate());

            addPlan.setVisibility(View.GONE);

            if (DateTimeUtils.getDateCount(selectedDate,DateTimeUtils.now(DATE_GLOBAL),"yyyy/MM/dd") >= 0) {

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

            tvStartVisitDate.setText(selectedDate);
            tvVisitEndDate.setText(selectedDate);

            tvStartVisitTime.setText(startTime);
            tvVisitEndTime.setText(endtime);

        }else {
            visitElementGroup.setVisibility(View.GONE);
            if (DateTimeUtils.getDateCount(selectedDate,DateTimeUtils.now(DATE_GLOBAL),"yyyy/MM/dd") >= 0)
                addPlan.setVisibility(View.VISIBLE);
            editPlan.setVisibility(View.GONE);
        }

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

        showMessage(getString(R.string.saved_successfully));
        dismiss();
    }

    @Override
    public void onDismiss() {
        dismiss();
    }
}
