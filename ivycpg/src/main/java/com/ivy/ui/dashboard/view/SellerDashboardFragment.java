package com.ivy.ui.dashboard.view;

import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.base.view.BaseFragment;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.view.HomeScreenActivity;
import com.ivy.ui.dashboard.SellerDashboardConstants;
import com.ivy.ui.dashboard.SellerDashboardContract;
import com.ivy.ui.dashboard.di.DaggerSellerDashboardComponent;
import com.ivy.ui.dashboard.di.SellerDashboardModule;
import com.ivy.utils.FontUtils;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import me.relex.circleindicator.CircleIndicator;

import static com.ivy.utils.AppUtils.isNullOrEmpty;

public class SellerDashboardFragment extends BaseFragment implements SellerDashboardContract.SellerDashboardView {

    @Inject
    SellerDashboardContract.SellerDashboardPresenter<SellerDashboardContract.SellerDashboardView> presenter;

    @BindView(R.id.distributorSpinner)
    ViewStub distributorSpinnerStub;

    @BindView(R.id.userSpinner)
    ViewStub userSpinnerStub;

    @BindView(R.id.dashSpinner)
    ViewStub dashSpinnerStub;

    @BindView(R.id.monthSpinner)
    ViewStub monthSpinnerStub;

    @BindView(R.id.weekSpinner)
    ViewStub weekSpinnerStub;

    @BindView(R.id.viewpager)
    ViewPager pager;

    @BindView(R.id.collapsing)
    CollapsingToolbarLayout collapsingToolbarLayout;

    @BindView(R.id.indicator)
    CircleIndicator circleIndicatorView;

    @BindView(R.id.dashboardLv)
    RecyclerView dashboardRecyclerView;

    @BindView(R.id.resultsHeaderTxt)
    TextView spinnerHeaderTxt;

    private String menuCode;

    private boolean isFromRetailer;
    private String type;

    @Override
    public void initializeDi() {

        DaggerSellerDashboardComponent.builder()
                .ivyAppComponent(((BusinessModel) getActivity().getApplication()).getComponent())
                .sellerDashboardModule(new SellerDashboardModule(this))
                .build().inject(this);

        setBasePresenter((BasePresenter) presenter);


    }

    @Override
    protected int setContentViewLayout() {
        return R.layout.fragment_seller_dashboard_new;
    }

    @Override
    public void initVariables(View view) {

    }

    @Override
    protected void getMessageFromAliens() {
        if (getArguments() != null) {
            Bundle bundle = getArguments();
            menuCode = bundle.getString("menuCode");
            isFromRetailer = bundle.getBoolean("isFromHomeScreenTwo", false);
            type = bundle.getString("type");
        }

        getDashSpinnerData();

    }

    private void getDashSpinnerData() {
        if (!isNullOrEmpty(type) && type.equalsIgnoreCase(SellerDashboardConstants.ROUTE)) {
            presenter.fetchSellerDashList(SellerDashboardConstants.DashBoardType.ROUTE);
        } else if (!isFromRetailer) {
            presenter.fetchSellerDashList(SellerDashboardConstants.DashBoardType.SELLER);
        } else
            presenter.fetchSellerDashList(SellerDashboardConstants.DashBoardType.RETAILER);

    }

    @Override
    protected void setUpViews() {

        setHasOptionsMenu(true);
        getActivity().invalidateOptionsMenu();

        spinnerHeaderTxt.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM, getActivity()));

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_target_plan, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_skutgt).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            if (isFromRetailer) {
                //update time stamp if previous screen is homescreentwo
                presenter.updateTimeStampModuleWise();
                presenter.saveModuleCompletion(menuCode);
            }
            getActivity().finish();
            return true;
        } else if (i == R.id.menu_next) {
            startActivityAndFinish(HomeScreenActivity.class);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void updateDashSpinner(ArrayList<String> dashList) {
        // Creating adapter for spinner

        if (!dashList.isEmpty()) {
            Spinner dashSpinner = (Spinner) dashSpinnerStub.inflate();
            dashSpinner.setVisibility(View.VISIBLE);
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(), R.layout.dashboard_spinner_layout, dashList);

            dataAdapter.setDropDownViewResource(R.layout.dashboard_spinner_list);

            dashSpinner.setAdapter(dataAdapter);
            dashSpinner.setOnItemSelectedListener(dashSpinnerSelectedListener);
        }

    }

    private AdapterView.OnItemSelectedListener dashSpinnerSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            String selectedInterval = adapterView.getSelectedItem().toString();
            if(!isFromRetailer){
                if(selectedInterval.equalsIgnoreCase(SellerDashboardConstants.P3M)){

                }
            }

        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };
}
