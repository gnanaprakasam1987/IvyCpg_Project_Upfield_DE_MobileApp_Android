package com.ivy.ui.dashboard.view;

import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
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
import com.ivy.cpg.primarysale.bo.DistributorMasterBO;
import com.ivy.cpg.view.dashboard.DashBoardBO;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.commons.KeyPairBoolData;
import com.ivy.sd.png.commons.MultiSpinner;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.commons.SpinnerListener;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.HomeScreenActivity;
import com.ivy.ui.dashboard.SellerDashboardConstants;
import com.ivy.ui.dashboard.SellerDashboardContract;
import com.ivy.ui.dashboard.adapter.DashboardListAdapter;
import com.ivy.ui.dashboard.di.DaggerSellerDashboardComponent;
import com.ivy.ui.dashboard.di.SellerDashboardComponent;
import com.ivy.ui.dashboard.di.SellerDashboardModule;
import com.ivy.utils.FontUtils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import me.relex.circleindicator.CircleIndicator;

import static com.ivy.utils.AppUtils.QT;
import static com.ivy.utils.AppUtils.isNullOrEmpty;

public class SellerDashboardFragment extends BaseFragment implements SellerDashboardContract.SellerDashboardView {

    @Inject
    SellerDashboardContract.SellerDashboardPresenter<SellerDashboardContract.SellerDashboardView> presenter;

    @BindView(R.id.dashSpinnerLayout)
    ConstraintLayout dashSpinnerLayout;

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

    @BindView(R.id.multiSelectStub)
    ViewStub multiSelectStub;

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

    private MultiSpinner distributorMultiSpinner;

    private MultiSpinner userMultiSpinner;

    private String menuCode;

    private boolean isFromRetailer;
    private String type;

    private SellerDashboardComponent sellerDashboardComponent;

    private String mSelectedDistributorId = "";

    private String mFilterUser;

    private DashboardListAdapter dashboardListAdapter;


    @Override
    public void initializeDi() {

        sellerDashboardComponent = DaggerSellerDashboardComponent.builder()
                .ivyAppComponent(((BusinessModel) getActivity().getApplication()).getComponent())
                .sellerDashboardModule(new SellerDashboardModule(this))
                .build();

        sellerDashboardComponent.inject(this);

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

        if (isFromRetailer) {
            getDashSpinnerData();
        } else {
            handleSellerDashboard();
        }


    }

    private void handleRetailerDashboard() {

    }

    private void handleSellerDashboard() {
        if (presenter.isUserBasedDash() && presenter.isDistributorBasedDash()) {
            if (presenter.isNiveaBasedDash()) {
                dashSpinnerLayout.setVisibility(View.GONE);
                multiSelectStub.setVisibility(View.VISIBLE);

                CoordinatorLayout multiSelectLayout = (CoordinatorLayout) multiSelectStub.inflate();

                distributorMultiSpinner = multiSelectLayout.findViewById(R.id.distributorSpinner1);
                userMultiSpinner = multiSelectLayout.findViewById(R.id.userSpinner1);

                presenter.fetchDistributorList();
            }

        }

    }

    @Override
    public void setupMultiSelectDistributorSpinner(ArrayList<DistributorMasterBO> distributors) {

        List<KeyPairBoolData> distArray = new ArrayList<>();
        distArray.add(new KeyPairBoolData(0, getResources().getString(R.string.all), true));

        int count = 0;

        for (int j = 0; j < distributors.size(); j++) {
            KeyPairBoolData h = new KeyPairBoolData();
            h.setId(SDUtil.convertToInt(distributors.get(j).getDId()));
            h.setName(distributors.get(j).getDName());
            h.setSelected(true);
            distArray.add(h);
            count++;
            mSelectedDistributorId += QT(distributors.get(j).getDId() + "");
            if (count != distributors.size())
                mSelectedDistributorId += ",";
        }

        distributorMultiSpinner.setItems(distArray, -1, new SpinnerListener() {
            @Override
            public void onItemsSelected(List<KeyPairBoolData> items) {
                int count = 0;
                mSelectedDistributorId = "";
                if (items.size() > 0) {
                    for (int i = 0; i < items.size(); i++) {
                        count++;
                        mSelectedDistributorId += QT(items.get(i).getId() + "");
                        if (count != items.size())
                            mSelectedDistributorId += ",";
                    }
                } else
                    mSelectedDistributorId = "0";


            }
        });

    }

    @Override
    public void setUpMultiSelectUserSpinner(ArrayList<UserMasterBO> users) {

        List<KeyPairBoolData> userArray = new ArrayList<>();

        userArray.add(new KeyPairBoolData(0, getString(R.string.all), true));
        int count = 0;
        for (int i = 0; i < users.size(); i++) {
            KeyPairBoolData h = new KeyPairBoolData();
            h.setId(users.get(i).getUserid());
            h.setName(users.get(i).getUserName());
            h.setSelected(true);
            userArray.add(h);
            count++;
            mFilterUser += QT(users.get(i).getUserid() + "");
            if (count != users.size())
                mFilterUser += ",";
        }

        userMultiSpinner.setItems(userArray, -1, new SpinnerListener() {
            @Override
            public void onItemsSelected(List<KeyPairBoolData> items) {
                Commons.print("Multi" + items.size());
                int count = 0;
                mFilterUser = "";
                for (int i = 0; i < items.size(); i++) {
                    count++;
                    mFilterUser += QT(items.get(i).getId() + "");
                    if (count != items.size())
                        mFilterUser += ",";
                }
                loadMultiSelectData();
            }
        });


    }

    @Override
    public void setDashboardListAdapter(ArrayList<DashBoardBO> dashBoardBOS) {

        dashboardListAdapter = new DashboardListAdapter(getActivity(), dashBoardBOS);

    }

    private void loadMultiSelectData() {

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

        dashboardRecyclerView.setAdapter(dashboardListAdapter);

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
            if (!isFromRetailer) {
                if (selectedInterval.equalsIgnoreCase(SellerDashboardConstants.P3M)) {

                }
            }

        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };
}
