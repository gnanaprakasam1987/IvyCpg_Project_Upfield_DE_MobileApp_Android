package com.ivy.ui.retailer.viewretailers.view.list;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import androidx.fragment.app.FragmentActivity;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.ivy.core.base.view.BaseFragment;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.ui.retailer.filter.RetailerPlanFilterBo;
import com.ivy.ui.retailer.filter.view.RetailerPlanFilterFragment;
import com.ivy.ui.retailer.viewretailers.RetailerContract;
import com.ivy.ui.retailer.viewretailers.adapter.RetailerListAdapter;
import com.ivy.ui.retailer.viewretailers.adapter.RetailerListClickListner;
import com.ivy.ui.retailer.viewretailers.di.DaggerRetailerComponent;
import com.ivy.ui.retailer.viewretailers.di.RetailerModule;
import com.ivy.ui.retailer.viewretailers.presenter.RetailerPresenterImpl;
import com.ivy.ui.retailerplan.addplan.DateWisePlanBo;
import com.ivy.ui.retailerplan.addplan.view.AddPlanDialogFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;

import static android.app.Activity.RESULT_OK;

public class RetailerListFragment extends BaseFragment implements RetailerContract.RetailerView, RetailerListClickListner {

    @BindView(R.id.rv_retailer)
    RecyclerView rvRetailer;

    private Context mContext;
    @Inject
    RetailerPresenterImpl<RetailerContract.RetailerView> presenter;

    private RetailerPlanFilterBo planFilterBo;
    private String mSelectedDate;
    private String mStartTime;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    @Override
    public void initializeDi() {
        DaggerRetailerComponent.builder()
                .ivyAppComponent(((BusinessModel) Objects.requireNonNull((FragmentActivity) mContext).getApplication()).getComponent())
                .retailerModule(new RetailerModule(this))
                .build()
                .inject(RetailerListFragment.this);

        setBasePresenter(presenter);
    }

    @Override
    protected int setContentViewLayout() {
        return R.layout.fragment_retailer_list;
    }

    @Override
    public void init(View view) {
        setHasOptionsMenu(true);

        ActionBar actionBar = ((AppCompatActivity) mContext).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rvRetailer.getContext(),
                linearLayoutManager.getOrientation());
        rvRetailer.addItemDecoration(dividerItemDecoration);
        rvRetailer.setLayoutManager(linearLayoutManager);
    }

    @Override
    protected void getMessageFromAliens() {
        if (Objects.requireNonNull(getActivity()).getIntent().getExtras() != null) {
            mSelectedDate = Objects.requireNonNull(getActivity().getIntent().getExtras()).getString("selectedDate", null);
            mStartTime = Objects.requireNonNull(getActivity().getIntent().getExtras()).getString("startTime", "");
        }
    }

    @Override
    protected void setUpViews() {
        presenter.fetchSelectedDateRetailerPlan(mSelectedDate,true);
    }

    @Override
    public void populateRetailers(List<RetailerMasterBO> retailerList) {
        rvRetailer.setAdapter(new RetailerListAdapter(mContext, retailerList, this));
    }

    @Override
    public void populateTodayPlannedRetailers(RetailerMasterBO retailerList) {

    }

    @Override
    public void populatePlannedRetailers(List<RetailerMasterBO> plannedRetailers) {

    }

    @Override
    public void populateUnPlannedRetailers(List<RetailerMasterBO> unPlannedRetailers) {

    }

    @Override
    public void populateCompletedRetailers(List<RetailerMasterBO> unPlannedRetailers) {

    }

    @Override
    public void updateView() {
        presenter.fetchUnPlannedRetailerList();
    }

    @Override
    public void drawRoutePath(String path) {

    }

    @Override
    public void focusMarker() {

    }

    private String searchText = "";

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_retailer_plan, menu);
        SearchManager searchManager = (SearchManager) mContext.getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        ImageView searchClose = searchView.findViewById(R.id.search_close_btn);
        searchClose.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);

        searchView.setSearchableInfo(searchManager != null ? searchManager.getSearchableInfo(((Activity) mContext).getComponentName()) : null);
        SearchView.OnQueryTextListener textChangeListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {


                presenter.prepareFilteredRetailerList(planFilterBo, newText.toLowerCase(), true);
                searchText = newText;

                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

        };
        searchView.setOnQueryTextListener(textChangeListener);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.map_retailer).setVisible(false);
        menu.findItem(R.id.calendar).setVisible(false);
        menu.findItem(R.id.filter).setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            ((Activity) mContext).finish();
            Objects.requireNonNull(getActivity()).overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            return true;
        }else if (item.getItemId() == R.id.filter) {

            RetailerPlanFilterFragment planFilterFragment = new RetailerPlanFilterFragment(mSelectedDate,planFilterBo);
            planFilterFragment.show(((FragmentActivity) mContext).getSupportFragmentManager(),
                    "filter_plan_fragment");

            return true;
        }

        return false;
    }

    @Override
    public void onRetailerSelected(RetailerMasterBO retailerMasterBO) {

        presenter.setRetailerMasterBo(retailerMasterBO);

        AddPlanDialogFragment addPlanDialogFragment;
        ArrayList<DateWisePlanBo> planList = presenter.getSelectedDateRetailerPlanList();
        if(mStartTime.length()==0)
        addPlanDialogFragment = new AddPlanDialogFragment(mSelectedDate, retailerMasterBO,
                presenter.getSelectedRetailerPlan(retailerMasterBO.getRetailerID())
                , planList);
        else
            addPlanDialogFragment = new AddPlanDialogFragment(mSelectedDate, retailerMasterBO,
                    presenter.getSelectedRetailerPlan(retailerMasterBO.getRetailerID())
                    , planList,mStartTime);
        addPlanDialogFragment.show(((FragmentActivity) mContext).getSupportFragmentManager(),
                "add_plan_fragment");
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

    @Subscribe
    public void onMessageEvent(Object obj) {

        if (obj instanceof RetailerPlanFilterBo) {

            planFilterBo = ((RetailerPlanFilterBo) obj);

            if (planFilterBo.getRetailerIds().isEmpty() && planFilterBo.getSortBy() == 0) {
                onMessageEvent("NODATA");
                return;
            }

            presenter.prepareFilteredRetailerList(((RetailerPlanFilterBo) obj), searchText.toLowerCase(), true);
        } else if (obj instanceof String) {
            if (((String) obj).equalsIgnoreCase("CLEAR")) {

                if (planFilterBo != null) {
                    planFilterBo = null;
                }

                presenter.fetchRetailerList();

            } else if ("NODATA".equalsIgnoreCase((String) obj)) {
                populateRetailers(new ArrayList<>());
            }
        }else if (obj instanceof DateWisePlanBo) {
            Intent intent = new Intent();
            Objects.requireNonNull(getActivity()).setResult(RESULT_OK, intent);
            getActivity().finish();
        }
    }


}
