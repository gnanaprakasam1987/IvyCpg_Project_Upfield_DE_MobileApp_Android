package com.ivy.ui.offlineplan.addplan.view;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.constraint.Group;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.ivy.cpg.view.profile.ProfileActivity;
import com.ivy.cpg.view.retailercontact.RetailerContactAvailBo;
import com.ivy.cpg.view.retailercontact.TimeSlotPickFragment;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.ui.offlineplan.addplan.AddPlanContract;
import com.ivy.ui.offlineplan.addplan.DateWisePlanBo;
import com.ivy.ui.offlineplan.addplan.di.AddPlanModule;
import com.ivy.ui.offlineplan.addplan.di.DaggerAddPlanComponent;
import com.ivy.ui.offlineplan.addplan.presenter.AddPlanPresenterImpl;
import com.ivy.ui.retailer.di.DaggerRetailerComponent;
import com.ivy.ui.retailer.di.RetailerModule;
import com.ivy.ui.retailer.view.map.RetailerMapFragment;
import com.ivy.utils.DateTimeUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.ivy.utils.DateTimeUtils.DATE_GLOBAL;
import static com.ivy.utils.DateTimeUtils.TIME_HOUR_MINS;

@SuppressLint("ValidFragment")
public class AddPlanDialogFragment extends BottomSheetDialogFragment implements AddPlanContract.AddPlanView {

    @BindView(R.id.add_plan)
    TextView addPlan;

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

    @BindView(R.id.visitElementGroup)
    Group visitElementGroup;

    @BindView(R.id.save_plan)
    TextView savePlan;

    private BottomSheetBehavior bottomSheetBehavior;

    private Context context;

    private RetailerMasterBO retailerMasterBO;

    private DateWisePlanBo dateWisePlanBo;

    private TimeSlotPickFragment timeSlotPickFragment;

    @Inject
    AddPlanPresenterImpl<AddPlanContract.AddPlanView> addPlanPresenter;

    public AddPlanDialogFragment(RetailerMasterBO retailerMaster, DateWisePlanBo dateWisePlanBo){
        retailerMasterBO = retailerMaster;
        this.dateWisePlanBo = dateWisePlanBo;
    }

    private String date="",startTime="",endtime="";

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    private Unbinder mUnBinder;

    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);

        View view = LayoutInflater.from(getContext()).inflate(R.layout.retailer_plan_info_layout, null);
        dialog.setContentView(view);

        initializeDi();

        mUnBinder = ButterKnife.bind(this, view);

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) view.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();

        addPlan.setOnClickListener(addToPlanListener);

        if (behavior != null && behavior instanceof BottomSheetBehavior) {

            bottomSheetBehavior = ((BottomSheetBehavior) behavior);

            ((BottomSheetBehavior) behavior).setState(BottomSheetBehavior.STATE_EXPANDED);

            ((BottomSheetBehavior) behavior).setBottomSheetCallback(bottomSheetCallBack);
        }

        date = DateTimeUtils.now(DATE_GLOBAL);
        startTime = DateTimeUtils.now(TIME_HOUR_MINS)+":00";
        endtime = DateTimeUtils.now(TIME_HOUR_MINS)+":00";

        if (!dateWisePlanBo.getDate().isEmpty())
            date = dateWisePlanBo.getDate();

        if (!dateWisePlanBo.getStartTime().isEmpty())
            startTime = DateTimeUtils.convertDateTimeObjectToRequestedFormat(dateWisePlanBo.getStartTime(),"yyyy/MM/dd HH:mm:ss","HH:mm");

        if (!dateWisePlanBo.getEndTime().isEmpty())
            endtime = DateTimeUtils.convertDateTimeObjectToRequestedFormat(dateWisePlanBo.getEndTime(),"yyyy/MM/dd HH:mm:ss","HH:mm");

        setPlanWindowValues();

    }

    private void initializeDi(){
        DaggerAddPlanComponent.builder()
                .ivyAppComponent(((BusinessModel) Objects.requireNonNull((FragmentActivity)context).getApplication()).getComponent())
                .addPlanModule(new AddPlanModule(this, context))
                .build()
                .inject(AddPlanDialogFragment.this);
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
                    if (!"Y".equalsIgnoreCase(retailerMasterBO.getIsVisited()))
                        addPlan.setVisibility(View.VISIBLE);

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

    @OnClick(R.id.profile_plan)
    void viewProfile(){
//        presenter.setRetailerMasterBo(retailerMasterBO);
        Intent i = new Intent(context, ProfileActivity.class);
        i.putExtra("From", "RetailerMap");
        i.putExtra("locvisit", true);
        i.putExtra("map", true);
        i.putExtra("HideVisit", retailerMasterBO.getIsToday() != 1);

        startActivity(i);
    }

    @OnClick(R.id.save_plan)
    void savePlan(){
        if (!"Y".equals(retailerMasterBO.getIsVisited())
                &&( retailerMasterBO.getIsToday() == 1
                || "Y".equals(retailerMasterBO.getIsDeviated()))) {
            addPlanPresenter.updatePlan(dateWisePlanBo.getDate(),tvStartVisitTime.getText().toString(),tvVisitEndTime.getText().toString(),retailerMasterBO);
        }else if(!"Y".equals(retailerMasterBO.getIsVisited())){
            addPlanPresenter.addNewPlan(dateWisePlanBo.getDate(),tvStartVisitTime.getText().toString(),tvVisitEndTime.getText().toString(),retailerMasterBO);
        }
        dismiss();
    }

    private View.OnClickListener addToPlanListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (addPlan.getText().toString().equalsIgnoreCase(getString(R.string.add_plan))){

                visitElementGroup.setVisibility(View.VISIBLE);

                addPlan.setVisibility(View.GONE);

                savePlan.setVisibility(View.VISIBLE);

                setViewBackground(ContextCompat.getDrawable(context,R.drawable.edittext_bottom_border));

            }else{

                addPlan.setVisibility(View.GONE);

                savePlan.setVisibility(View.VISIBLE);

                setViewBackground(ContextCompat.getDrawable(context,R.drawable.edittext_bottom_border));

                tvStartVisitTime.setOnClickListener(startVisitTimeListener);
                tvVisitEndTime.setOnClickListener(startVisitTimeListener);
            }

            tvStartVisitDate.setText(date);
            tvVisitEndDate.setText(date);

            tvStartVisitTime.setText(startTime);
            tvVisitEndTime.setText(endtime);

            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED)
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
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

            if (!"Y".equals(retailerMasterBO.getIsVisited())) {
                addPlan.setVisibility(View.VISIBLE);
                addPlan.setText(getString(R.string.edit));
            }else
                addPlan.setVisibility(View.GONE);

            tvStartVisitDate.setText(date);
            tvVisitEndDate.setText(date);

            tvStartVisitTime.setText(startTime);
            tvVisitEndTime.setText(endtime);

        }else {
            visitElementGroup.setVisibility(View.GONE);
            addPlan.setVisibility(View.VISIBLE);
            addPlan.setText(getString(R.string.add_plan));
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
    public void onDestroyView() {
        super.onDestroyView();
        try {
            mUnBinder.unbind();
            mUnBinder = null;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void showLoading() {

    }

    @Override
    public void showLoading(String message) {

    }

    @Override
    public void showLoading(int strinRes) {

    }

    @Override
    public void hideLoading() {

    }

    @Override
    public void onError(int resId) {

    }

    @Override
    public void onError(String message) {

    }

    @Override
    public void showMessage(String message) {

    }

    @Override
    public void showMessage(int resId) {

    }

    @Override
    public boolean isNetworkConnected() {
        return false;
    }

    @Override
    public void hideKeyboard() {

    }

    @Override
    public void setLayoutDirection(int direction) {

    }

    @Override
    public void handleLayoutDirection(String language) {

    }

    @Override
    public void setBlueTheme() {

    }

    @Override
    public void setRedTheme() {

    }

    @Override
    public void setOrangeTheme() {

    }

    @Override
    public void setGreenTheme() {

    }

    @Override
    public void setPinkTheme() {

    }

    @Override
    public void setNavyBlueTheme() {

    }

    @Override
    public void setFontSize(String fontSize) {

    }

    @Override
    public void showAlert(String title, String msg) {

    }

    @Override
    public void showAlert(String title, String msg, CommonDialog.PositiveClickListener positiveClickListener) {

    }

    @Override
    public void showAlert(String title, String msg, CommonDialog.PositiveClickListener positiveClickListener, CommonDialog.negativeOnClickListener negativeOnClickListener) {

    }

    @Override
    public void showAlert(String title, String msg, CommonDialog.PositiveClickListener positiveClickListener, boolean isCancelable) {

    }

    @Override
    public void createNFCManager() {

    }

    @Override
    public void resumeNFCManager() {

    }

    @Override
    public void pauseNFCManager() {

    }

    @Override
    public void setScreenTitle(String title) {

    }

    @Override
    public void setUpToolbar(String title) {

    }

    @Override
    public void showUpdatedSuccessfullyMessage() {
    }

    @Override
    public void showUpdateFailureMessage() {

    }
}
