package com.ivy.ui.announcement.view;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.base.view.BaseFragment;
import com.ivy.cpg.view.profile.HorizontalDividerItemDecoration;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.ui.announcement.AnnouncementConstant;
import com.ivy.ui.announcement.AnnouncementContract;
import com.ivy.ui.announcement.adapter.AnnouncementAdapter;
import com.ivy.ui.announcement.di.AnnouncementModel;
import com.ivy.ui.announcement.di.DaggerAnnouncementComponent;
import com.ivy.ui.announcement.model.AnnouncementBo;
import com.ivy.ui.notes.NoteConstant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;

public class AnnouncementFragment extends BaseFragment implements AnnouncementContract.AnnouncementView {

    private String screenTitle;
    private Context mContext;
    private boolean isFromHomeSrc = false;

    @BindView(R.id.announced_recycler_view)
    RecyclerView announcementRecyclerView;

    @BindView(R.id.no_data_tv)
    TextView noDataTv;

    @Inject
    AnnouncementContract.AnnouncementPresenter<AnnouncementContract.AnnouncementView> announcementPresenter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    @Override
    public void initializeDi() {

        DaggerAnnouncementComponent
                .builder()
                .ivyAppComponent(((BusinessModel) getActivity().getApplication()).getComponent())
                .announcementModel(new AnnouncementModel(this))
                .build()
                .inject(this);

        setBasePresenter((BasePresenter) announcementPresenter);

    }

    @Override
    protected int setContentViewLayout() {
        return R.layout.announcement_fragment;
    }

    @Override
    public void init(View view) {

    }

    @Override
    protected void getMessageFromAliens() {
        Bundle bundle = getArguments();
        if (bundle == null)
            bundle = Objects.requireNonNull(getActivity()).getIntent().getExtras();

        if (bundle != null) {
            if (bundle.containsKey(AnnouncementConstant.SCREEN_TITLE))
                screenTitle = bundle.getString(NoteConstant.SCREEN_TITLE, getString(R.string.announcement));

            if (bundle.containsKey(AnnouncementConstant.FROM_HOME_SCREEN))
                isFromHomeSrc = true;
        }
    }

    @Override
    protected void setUpViews() {
        setUpToolbar(screenTitle);
        setHasOptionsMenu(true);
        setUpRecyclerView();

        announcementPresenter.fetchData(isFromHomeSrc);
    }


    private void setUpRecyclerView() {
        announcementRecyclerView.setHasFixedSize(true);
        announcementRecyclerView.setNestedScrollingEnabled(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        announcementRecyclerView.setLayoutManager(layoutManager);
        announcementRecyclerView.
                addItemDecoration(
                        new HorizontalDividerItemDecoration
                                .Builder(getActivity()).size(1)
                                .color(ContextCompat.getColor(mContext, R.color.light_gray))
                                .margin(getResources().getDimensionPixelSize(R.dimen._24sdp),
                                        getResources().getDimensionPixelSize(R.dimen._24sdp))
                                .build());
    }

    @Override
    public void onDataNotMappedMsg() {
        noDataTv.setText(getString(R.string.data_not_mapped));
        announcementRecyclerView.setVisibility(View.GONE);
    }


    @Override
    public void updateLabelNames(HashMap<String, String> labelMap) {

    }

    @Override
    public void updateListData(ArrayList<AnnouncementBo> announcementBoArrayList) {
        announcementRecyclerView.setAdapter(new AnnouncementAdapter(mContext, announcementBoArrayList));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {

            Objects.requireNonNull(getActivity()).finish();
            getActivity().overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
        }
        return super.onOptionsItemSelected(item);
    }

}
