package com.ivy.ui.task.view;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.base.view.BaseFragment;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.cpg.view.basedi.BaseModule;
import com.ivy.cpg.view.basedi.DaggerBaseComponent;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.ui.task.FilterViewListener;
import com.ivy.ui.task.TaskConstant;
import com.ivy.ui.task.adapter.filter.FilterItemAdapter;
import com.ivy.ui.task.adapter.filter.FilterMenuAdapter;
import com.ivy.ui.task.model.FilterBo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import javax.inject.Inject;

public class FilterFragment extends BaseFragment implements FilterMenuAdapter.UpdateListener, View.OnClickListener, BaseIvyView {

    private Context context;
    private RecyclerView levelSelectionListView;
    private RecyclerView filterItemSelectionListView;
    private String lastMenuSelectedPos = "";
    private String lastSelectedMenuName;
    private HashMap<String, ArrayList<FilterBo>> hasMapFilterList;
    private ArrayList<String> menuList;
    private boolean isFromHomeSrc = false;
    private FilterViewListener filterViewListener;
    private HashMap<String, ArrayList<Object>> selectedIdsHashMap = new HashMap<>();
    private FilterMenuAdapter filterMenuAdapter;

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
        return R.layout.filter_fragment;
    }

    @Override
    public void init(View view) {
        levelSelectionListView = view.findViewById(R.id.filterlistview);
        filterItemSelectionListView = view.findViewById(R.id.filtergridview);
        view.findViewById(R.id.btn_ok).setOnClickListener(this);
        view.findViewById(R.id.btn_cancel).setOnClickListener(this);
    }

    @Override
    protected void getMessageFromAliens() {
        Bundle bundle = getArguments();
        if (bundle == null)
            bundle = Objects.requireNonNull(getActivity()).getIntent().getExtras();

        if (bundle != null) {

            if (bundle.containsKey(TaskConstant.FROM_HOME_SCREEN))
                isFromHomeSrc = true;

            if (bundle.containsKey("SelectedFilterList")
                    && bundle.get("SelectedFilterList") != null) {
                selectedIdsHashMap = ((HashMap<String, ArrayList<Object>>) bundle.getSerializable("SelectedFilterList"));
            }

            if (bundle.containsKey("hashList")) {
                hasMapFilterList = new HashMap<>();
                hasMapFilterList = (HashMap<String, ArrayList<FilterBo>>) bundle.getSerializable("hashList");
            }

            if (bundle.containsKey("menuList")
                    && bundle.getStringArrayList("menuList") != null) {
                menuList = new ArrayList<>();
                menuList.addAll(bundle.getStringArrayList("menuList"));
            }
        }
    }

    @Override
    protected void setUpViews() {

        setUpRecyclerView();

        if (isFromHomeSrc)
            lastSelectedMenuName = "Retailer";

        if (!hasMapFilterList.isEmpty()) {
            if (menuList == null
                    || menuList.isEmpty()) {
                menuList = new ArrayList<>();
                menuList.addAll(hasMapFilterList.keySet());
            }

            if (!menuList.isEmpty()) {
                filterMenuAdapter = new FilterMenuAdapter(context, menuList, this, selectedIdsHashMap);
                levelSelectionListView.setAdapter(filterMenuAdapter);
            }
        }


    }

    private void setUpRecyclerView() {
        levelSelectionListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        levelSelectionListView.setItemAnimator(new DefaultItemAnimator());
        levelSelectionListView.setHasFixedSize(false);
        levelSelectionListView.setNestedScrollingEnabled(false);
        levelSelectionListView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));


        filterItemSelectionListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        filterItemSelectionListView.setItemAnimator(new DefaultItemAnimator());
        filterItemSelectionListView.setHasFixedSize(false);
        filterItemSelectionListView.setNestedScrollingEnabled(false);
        filterItemSelectionListView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
    }


    @Override
    public void updateListAdapter(String menuName, HashMap<String, ArrayList<Object>> selectedIdsHashMap) {
        lastSelectedMenuName = menuName;
        this.selectedIdsHashMap = selectedIdsHashMap;
        filterItemSelectionListView.setAdapter(new FilterItemAdapter(context, hasMapFilterList.get(lastSelectedMenuName), lastSelectedMenuName, this, selectedIdsHashMap));
    }

    @Override
    public void updateSelectedId(String selectedKey, Object filterIds, boolean isChecked) {
        ArrayList<Object> selectedIdsList;
        if (selectedIdsHashMap == null)
            selectedIdsHashMap = new HashMap<>();

        if (isChecked
                && (selectedIdsHashMap.get(selectedKey) == null)) {
            selectedIdsList = new ArrayList<>();
            selectedIdsList.add(filterIds);
            selectedIdsHashMap.put(selectedKey, selectedIdsList);
        } else if (isChecked
                && selectedIdsHashMap.get(selectedKey) != null
                && !selectedIdsHashMap.get(selectedKey).contains(filterIds)) {
            selectedIdsHashMap.get(selectedKey).add(filterIds);
        } else {
            selectedIdsHashMap.get(selectedKey).remove(filterIds);
        }

        if (selectedIdsHashMap.get(selectedKey).isEmpty()) {
            selectedIdsHashMap.remove(selectedKey);
            filterMenuAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.btn_ok:
                filterViewListener.apply(menuList, hasMapFilterList, selectedIdsHashMap, lastSelectedMenuName);
                break;

            case R.id.btn_cancel:
                selectedIdsHashMap = new HashMap<>();
                filterViewListener.clearAll();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    public void setFilterViewListener(FilterViewListener filterViewListener) {
        this.filterViewListener = filterViewListener;
    }
}
