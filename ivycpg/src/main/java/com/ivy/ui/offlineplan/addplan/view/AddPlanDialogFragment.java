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
import android.widget.Toast;

import com.ivy.cpg.view.profile.ProfileActivity;
import com.ivy.cpg.view.retailercontact.RetailerContactAvailBo;
import com.ivy.cpg.view.retailercontact.TimeSlotPickFragment;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.utils.DateTimeUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.ivy.utils.DateTimeUtils.DATE_GLOBAL;
import static com.ivy.utils.DateTimeUtils.TIME;
import static com.ivy.utils.DateTimeUtils.TIME_HOUR_MINS;

@SuppressLint("ValidFragment")
public class AddPlanDialogFragment extends BottomSheetDialogFragment {

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

    private TimeSlotPickFragment timeSlotPickFragment;

    public AddPlanDialogFragment(RetailerMasterBO retailerMaster){
        retailerMasterBO = retailerMaster;
    }

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

        mUnBinder = ButterKnife.bind(this, view);

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) view.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();

        addPlan.setOnClickListener(addToPlanListener);

        if (behavior != null && behavior instanceof BottomSheetBehavior) {

            bottomSheetBehavior = ((BottomSheetBehavior) behavior);

            ((BottomSheetBehavior) behavior).setState(BottomSheetBehavior.STATE_EXPANDED);

            ((BottomSheetBehavior) behavior).setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
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

                            if (savePlan.getVisibility() == View.VISIBLE)
                                tvStartVisitTime.setOnClickListener(startVisitTimeListener);
                            else
                                tvStartVisitTime.setOnClickListener(null);

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
            });
        }

        setPlanWindowValues();

    }

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

                tvStartVisitDate.setText(DateTimeUtils.now(DATE_GLOBAL));
                tvVisitEndDate.setText(DateTimeUtils.now(DATE_GLOBAL));

                tvStartVisitTime.setText(DateTimeUtils.now(TIME_HOUR_MINS)+":00");
                tvVisitEndTime.setText(DateTimeUtils.now(TIME_HOUR_MINS)+":00");

            }else{

                addPlan.setVisibility(View.GONE);

                savePlan.setVisibility(View.VISIBLE);

                setViewBackground(ContextCompat.getDrawable(context,R.drawable.edittext_bottom_border));

                tvStartVisitDate.setText(DateTimeUtils.now(DATE_GLOBAL));
                tvVisitEndDate.setText(DateTimeUtils.now(DATE_GLOBAL));

                tvStartVisitTime.setText(DateTimeUtils.now(TIME_HOUR_MINS)+":00");
                tvVisitEndTime.setText(DateTimeUtils.now(TIME_HOUR_MINS)+":00");

                tvStartVisitTime.setOnClickListener(startVisitTimeListener);
                tvVisitEndTime.setOnClickListener(startVisitTimeListener);
            }

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

        }else {
            visitElementGroup.setVisibility(View.GONE);
            addPlan.setVisibility(View.VISIBLE);
            addPlan.setText(getString(R.string.add_plan));
        }

        tvOutletName.setText(retailerMasterBO.getRetailerName());
        tvOutletAddress.setText(retailerMasterBO.getAddress1());

        tvStartVisitDate.setText(DateTimeUtils.now(DATE_GLOBAL));
        tvVisitEndDate.setText(DateTimeUtils.now(DATE_GLOBAL));

        tvStartVisitTime.setText(DateTimeUtils.now(TIME_HOUR_MINS)+":00");
        tvVisitEndTime.setText(DateTimeUtils.now(TIME_HOUR_MINS)+":00");
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
}
