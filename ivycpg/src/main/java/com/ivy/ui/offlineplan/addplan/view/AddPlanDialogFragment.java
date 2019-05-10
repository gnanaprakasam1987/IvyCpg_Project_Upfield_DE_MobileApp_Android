package com.ivy.ui.offlineplan.addplan.view;

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
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.core.base.view.BaseBottomSheetDialogFragment;
import com.ivy.cpg.view.profile.ProfileActivity;
import com.ivy.cpg.view.retailercontact.RetailerContactAvailBo;
import com.ivy.cpg.view.retailercontact.TimeSlotPickFragment;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.ui.offlineplan.addplan.AddPlanContract;
import com.ivy.ui.offlineplan.addplan.DateWisePlanBo;
import com.ivy.ui.offlineplan.addplan.di.AddPlanModule;
import com.ivy.ui.offlineplan.addplan.di.DaggerAddPlanComponent;
import com.ivy.ui.offlineplan.addplan.presenter.AddPlanPresenterImpl;
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

import static com.ivy.utils.DateTimeUtils.DATE_GLOBAL;
import static com.ivy.utils.DateTimeUtils.TIME_HOUR_MINS;

@SuppressLint("ValidFragment")
public class AddPlanDialogFragment extends BaseBottomSheetDialogFragment implements AddPlanContract.AddPlanView {

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

    public AddPlanDialogFragment(RetailerMasterBO retailerMaster,
                                 DateWisePlanBo dateWisePlanBo, ArrayList<DateWisePlanBo> planList){
        retailerMasterBO = retailerMaster;
        this.dateWisePlanBo = dateWisePlanBo;
        this.planList = planList;
    }

    private String date="",startTime="",endtime="";

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

        date = DateTimeUtils.now(DATE_GLOBAL);
        startTime = DateTimeUtils.now(TIME_HOUR_MINS)+":00";
        endtime = DateTimeUtils.now(TIME_HOUR_MINS)+":00";

        if (dateWisePlanBo != null && !dateWisePlanBo.getDate().isEmpty())
            date = dateWisePlanBo.getDate();

        if (dateWisePlanBo != null && !dateWisePlanBo.getStartTime().isEmpty())
            startTime = DateTimeUtils.convertDateTimeObjectToRequestedFormat(dateWisePlanBo.getStartTime(),"yyyy/MM/dd HH:mm:ss","HH:mm");

        if (dateWisePlanBo != null && !dateWisePlanBo.getEndTime().isEmpty())
            endtime = DateTimeUtils.convertDateTimeObjectToRequestedFormat(dateWisePlanBo.getEndTime(),"yyyy/MM/dd HH:mm:ss","HH:mm");

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
                    if (!"Y".equalsIgnoreCase(retailerMasterBO.getIsVisited())) {
                        addPlan.setVisibility(View.VISIBLE);
                        editPlan.setVisibility(View.VISIBLE);
                    }

                    if (!"Y".equalsIgnoreCase(retailerMasterBO.getIsVisited())
                            && retailerMasterBO.getIsToday() != 1 && !"Y".equalsIgnoreCase(retailerMasterBO.getIsDeviated()))
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
        i.putExtra("locvisit", true);
        i.putExtra("map", true);
        i.putExtra("HideVisit", retailerMasterBO.getIsToday() != 1);

        startActivity(i);
    }

    @OnClick(R.id.tv_save)
    void savePlan(){

        String startTime = tvStartVisitTime.getText().toString();
        String endTime = tvVisitEndTime.getText().toString();

        if (validate(startTime,endTime)) {

            if (!"Y".equals(retailerMasterBO.getIsVisited())
                    && (retailerMasterBO.getIsToday() == 1
                    || "Y".equals(retailerMasterBO.getIsDeviated())) && dateWisePlanBo != null) {
                addPlanPresenter.updatePlan(dateWisePlanBo.getDate(), startTime, endtime, retailerMasterBO);
            } else if (!"Y".equals(retailerMasterBO.getIsVisited())) {
                addPlanPresenter.addNewPlan(date, startTime, endTime, retailerMasterBO);
            }

            dismiss();
        }
    }

    private boolean validate(String startTime, String endtime){

        for (DateWisePlanBo planBo : planList) {
            if (DateTimeUtils.isBetweenTime(startTime, endtime, planBo.getStartTime(), true)) {
                showMessage(getString(R.string.time_slot_already_selected));
                return false;
            } else if (DateTimeUtils.isBetweenTime(startTime, endtime, planBo.getEndTime(), false)) {
                showMessage(getString(R.string.time_slot_already_selected));
                return false;
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
        addPlanPresenter.cancelPlan(dateWisePlanBo);
        dismiss();
    }

    @OnClick(R.id.tv_delete)
    void deletePlan(){
        addPlanPresenter.deletePlan(dateWisePlanBo);
        dismiss();
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

            tvStartVisitDate.setText(date);
            tvVisitEndDate.setText(date);

            tvStartVisitTime.setText(startTime);
            tvVisitEndTime.setText(endtime);

            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED)
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

            if (planList != null && !planList.isEmpty()){

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

        if ("Y".equals(retailerMasterBO.getIsVisited())
                || retailerMasterBO.getIsToday() == 1
                || "Y".equals(retailerMasterBO.getIsDeviated())) {

            visitElementGroup.setVisibility(View.VISIBLE);

            tvLastVisitDate.setText(retailerMasterBO.getLastVisitDate());

            addPlan.setVisibility(View.GONE);

            if (dateWisePlanBo == null){
                cancelPlan.setVisibility(View.GONE);
                deletePlan.setVisibility(View.GONE);
            }else if (dateWisePlanBo.isServerData() && !"Y".equals(retailerMasterBO.getIsVisited())){
                cancelPlan.setVisibility(View.VISIBLE);
                deletePlan.setVisibility(View.GONE);
            }else if (!dateWisePlanBo.isServerData() && !"Y".equals(retailerMasterBO.getIsVisited())){
                cancelPlan.setVisibility(View.GONE);
                deletePlan.setVisibility(View.VISIBLE);
            }

            if (!"Y".equals(retailerMasterBO.getIsVisited())) {
                editPlan.setVisibility(View.VISIBLE);
            }else {
                editPlan.setVisibility(View.GONE);
            }

            tvStartVisitDate.setText(date);
            tvVisitEndDate.setText(date);

            tvStartVisitTime.setText(startTime);
            tvVisitEndTime.setText(endtime);

        }else {
            visitElementGroup.setVisibility(View.GONE);
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
}
