package com.ivy.ui.reports.syncreport.view;

import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.base.view.BaseFragment;
import com.ivy.ui.reports.syncreport.SyncReportContract;
import com.ivy.ui.reports.syncreport.adapter.SyncReportExpandableAdapter;
import com.ivy.ui.reports.syncreport.di.DaggerSyncReportComponent;
import com.ivy.ui.reports.syncreport.di.SyncReportModule;
import com.ivy.ui.reports.syncreport.model.SyncReportBO;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;

import java.util.ArrayList;
import java.util.HashMap;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SyncReportDownloadFragment extends BaseFragment implements SyncReportContract.SyncReportView {

    @Inject
    SyncReportContract.SyncReportPresenter<SyncReportContract.SyncReportView> syncReportPresenter;

    @BindView(R.id.list_exp)
    ExpandableListView expListView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void initializeDi() {

        DaggerSyncReportComponent.builder()
                .syncReportModule(new SyncReportModule(this))
                .ivyAppComponent(((BusinessModel) getActivity().getApplicationContext()).getComponent())
                .build()
                .inject(this);

        setBasePresenter((BasePresenter) syncReportPresenter);
    }

    @Override
    protected int setContentViewLayout() {
        return R.layout.sync_report_download_fragment;
    }

    @Override
    public void init(View view) {

    }

    @Override
    protected void getMessageFromAliens() {

    }

    @Override
    protected void setUpViews() {
        setUnBinder(ButterKnife.bind(getActivity()));
        syncReportPresenter.fetchData();
    }

    @Override
    public void setApiDownloadDetails(HashMap<String, ArrayList<SyncReportBO>> downloadApiDetailsMap, ArrayList<SyncReportBO> apiList) {

        expListView.setCacheColorHint(0);
        expListView.setAdapter(new SyncReportExpandableAdapter(getActivity(), downloadApiDetailsMap, apiList));
    }

    @Override
    public void showDataNotMappedMsg() {
        showMessage(R.string.data_not_mapped);
    }
}
