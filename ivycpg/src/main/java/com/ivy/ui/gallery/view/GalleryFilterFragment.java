package com.ivy.ui.gallery.view;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.base.view.BaseFragment;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.cpg.view.basedi.BaseModule;
import com.ivy.cpg.view.basedi.DaggerBaseComponent;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.ui.gallery.GalleryFilterListener;
import com.ivy.ui.gallery.adapter.GalleryFilterAdapter;

import java.util.ArrayList;
import java.util.Objects;

import javax.inject.Inject;

import static com.ivy.ui.gallery.GalleryConstant.FILTERED_LIST;
import static com.ivy.ui.gallery.GalleryConstant.SECTION_LIST;

public class GalleryFilterFragment extends BaseFragment implements View.OnClickListener, GalleryFilterAdapter.UpdateListener {
    private Context context;
    private RecyclerView filterItemSelectionListView;
    private ArrayList<String> sectionList;
    private ArrayList<String> selectedFilterList = new ArrayList<>();
    private GalleryFilterListener filterViewListener;

    @Inject
    BaseIvyPresenter<BaseIvyView> viewBasePresenter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void initializeDi() {
        DaggerBaseComponent.builder()
                .baseModule(new BaseModule(this))
                .ivyAppComponent(((BusinessModel) getActivity().getApplication()).getComponent())
                .build()
                .inject(this);

        setBasePresenter((BasePresenter) viewBasePresenter);
    }

    @Override
    protected int setContentViewLayout() {
        return R.layout.fragment_gallery_filter;
    }

    @Override
    public void init(View view) {
        filterItemSelectionListView = view.findViewById(R.id.filterlistview);
        view.findViewById(R.id.btn_ok).setOnClickListener(this);
        view.findViewById(R.id.btn_cancel).setOnClickListener(this);
    }

    @Override
    protected void getMessageFromAliens() {
        Bundle bundle = getArguments();
        if (bundle == null)
            bundle = Objects.requireNonNull(getActivity()).getIntent().getExtras();

        if (bundle != null) {
            if (bundle.containsKey(SECTION_LIST)
                    && bundle.getStringArrayList(SECTION_LIST) != null) {
                sectionList = new ArrayList<>();
                sectionList.addAll(bundle.getStringArrayList(SECTION_LIST));
            }

            if (bundle.containsKey(FILTERED_LIST)
                    && bundle.getStringArrayList(FILTERED_LIST) != null) {
                selectedFilterList = new ArrayList<>();
                selectedFilterList.addAll(bundle.getStringArrayList(FILTERED_LIST));
            }
        }
    }

    @Override
    protected void setUpViews() {
        setUpRecyclerView();

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_ok:
                if (selectedFilterList.isEmpty())
                    showMessage(getString(R.string.choose_any_one_option));
                else
                    filterViewListener.apply(selectedFilterList);
                break;

            case R.id.btn_cancel:
                selectedFilterList.clear();
                filterViewListener.clearAll();
                break;
        }

    }

    private void setUpRecyclerView() {
        filterItemSelectionListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        filterItemSelectionListView.setItemAnimator(new DefaultItemAnimator());
        filterItemSelectionListView.setHasFixedSize(false);
        filterItemSelectionListView.setNestedScrollingEnabled(false);
        filterItemSelectionListView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        filterItemSelectionListView.setAdapter(new GalleryFilterAdapter(getActivity(), sectionList, this, selectedFilterList));
    }

    @Override
    public void updateListAdapter(String menuName, boolean isChecked) {
        if (isChecked) {
            if (!selectedFilterList.contains(menuName))
                selectedFilterList.add(menuName);
        } else {
            selectedFilterList.remove(menuName);
        }
    }

    public void setFilterViewListener(GalleryFilterListener filterViewListener) {
        this.filterViewListener = filterViewListener;
    }
}

