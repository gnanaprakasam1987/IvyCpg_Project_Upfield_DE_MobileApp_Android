package com.ivy.ui.retailerplanfilter.view;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ivy.core.base.view.BaseBottomSheetDialogFragment;
import com.ivy.cpg.view.homescreen.HomeScreenActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.ui.retailerplanfilter.RetailerPlanFilterContract;
import com.ivy.ui.retailerplanfilter.di.DaggerRetailerPlanFilterComponent;
import com.ivy.ui.retailerplanfilter.di.RetailerPlanFilterComponent;
import com.ivy.ui.retailerplanfilter.di.RetailerPlanFilterModule;
import com.ivy.ui.retailerplanfilter.presenter.RetailerPlanFilterPresenterImpl;

import java.util.Objects;

import javax.inject.Inject;

public class RetailerPlanFilterFragment extends BaseBottomSheetDialogFragment implements RetailerPlanFilterContract.RetailerPlanFilterView{

    private BottomSheetBehavior bottomSheetBehavior;

    @Inject
    RetailerPlanFilterPresenterImpl<RetailerPlanFilterContract.RetailerPlanFilterView> presenter;

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

        setHasOptionsMenu(true);

        ActionBar actionBar = ((AppCompatActivity) context).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        setScreenTitle(getString(R.string.filter_by));

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) view.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();

        if (behavior != null && behavior instanceof BottomSheetBehavior) {

            bottomSheetBehavior = ((BottomSheetBehavior) behavior);

            bottomSheetBehavior.setPeekHeight(1000);

            ((BottomSheetBehavior) behavior).setState(BottomSheetBehavior.STATE_EXPANDED);

            ((BottomSheetBehavior) behavior).setBottomSheetCallback(bottomSheetCallBack);
        }
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

}
