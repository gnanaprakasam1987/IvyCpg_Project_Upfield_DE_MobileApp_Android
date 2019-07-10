package com.ivy.ui.reports.syncreport.view;

import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.base.view.BaseFragment;
import com.ivy.ui.reports.syncreport.SyncUploadReportContract;
import com.ivy.ui.reports.syncreport.adapter.SyncUploadReportAdapter;
import com.ivy.ui.reports.syncreport.di.DaggerSyncUploadReportComponent;
import com.ivy.ui.reports.syncreport.di.SyncUploadReportModule;
import com.ivy.ui.reports.syncreport.model.SyncReportBO;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SyncReportUploadFragment extends BaseFragment implements SyncUploadReportContract.SyncUploadReportView {

    @Inject
    SyncUploadReportContract.SyncUploadReportPresenter<SyncUploadReportContract.SyncUploadReportView> syncUploadReportPresenter;

    @BindView(R.id.rc_report)
    RecyclerView recyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void initializeDi() {

        DaggerSyncUploadReportComponent.builder()
                .syncUploadReportModule(new SyncUploadReportModule(this))
                .ivyAppComponent(((BusinessModel) getActivity().getApplicationContext()).getComponent())
                .build()
                .inject(this);

        setBasePresenter((BasePresenter) syncUploadReportPresenter);
    }

    @Override
    protected int setContentViewLayout() {
        return R.layout.sync_report_upload_fragment;
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
        syncUploadReportPresenter.fetchData();
    }

    @Override
    public void setData(ArrayList<SyncReportBO> dataList) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(new SyncUploadReportAdapter(getActivity(), dataList));
    }

    @Override
    public void showDataNotMappedMsg() {
        showMessage(R.string.data_not_mapped);
    }
}
