package com.ivy.ui.reports.dynamicreport.view;

import android.app.AlertDialog;
import android.arch.lifecycle.LifecycleObserver;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.base.view.BaseFragment;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.ui.reports.dynamicreport.DynamicReportContract;
import com.ivy.ui.reports.dynamicreport.adapter.DynamicReportPagerAdapter;
import com.ivy.ui.reports.dynamicreport.di.DaggerDynamicReportComponent;
import com.ivy.ui.reports.dynamicreport.di.DynamicReportModule;
import com.ivy.ui.reports.dynamicreport.model.DynamicReportBO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.res.Configuration.SCREENLAYOUT_SIZE_LARGE;

public class DynamicReportFragmentNew extends BaseFragment implements DynamicReportContract.DynamicReportView, ColumnSearchDialog.MenuIconListener, LifecycleObserver {

    private String screenTitle;
    private String menuCode = "";
    private String retailerId = "0";
    private boolean is7InchTablet;

    @Inject
    DynamicReportContract.DynamicReportPresenter<DynamicReportContract.DynamicReportView> dynamicReportPresenter;

    @BindView(R.id.tabs)
    TabLayout tab;

    @BindView(R.id.viewPager)
    DynamicReportViewPager viewPager;

    DynamicReportPagerAdapter adapter;

    ArrayAdapter<String> columnAdapter;

    int selectedColumnCount = 0;
    private Menu menu;
    private HashMap<String, HashMap<String, DynamicReportBO>> fieldsMap;
    ArrayList<String> headerList;
    private boolean isClearMenu = true;

    DynamicReportTabFragment mCurrentFragment;

    public interface DialogListener {
        void onColumnHide();

        void onColumnSearch(boolean isClear);
    }

    @Override
    public void initializeDi() {

        DaggerDynamicReportComponent.builder()
                .dynamicReportModule(new DynamicReportModule(this))
                .ivyAppComponent(((BusinessModel) getActivity().getApplicationContext()).getComponent())
                .build()
                .inject(this);

        setBasePresenter((BasePresenter) dynamicReportPresenter);
    }

    @Override
    protected int setContentViewLayout() {
        return R.layout.dynamic_report_fragment_new;
    }

    @Override
    public void init(View view) {
        setLocationAdapter();
        for (int i = 0; i <= 2; i++) {
            columnAdapter.add(String.valueOf(i));
        }
        viewPager.setPagingEnabled(false);
    }

    @Override
    protected void getMessageFromAliens() {
        if (getArguments() != null) {
            screenTitle = getArguments().getString("screentitle");
            menuCode = getArguments().getString("menucode");
            retailerId = getArguments().getString("rid", "0");
        } else if (getActivity() != null ){
            screenTitle = getActivity().getIntent().getStringExtra("screentitle");
            menuCode = getActivity().getIntent().getStringExtra("menucode");
            retailerId = getActivity().getIntent().getStringExtra("rid");
        }
    }

    @Override
    protected void setUpViews() {
        setUpActionBar();
        is7InchTablet = getResources().getConfiguration().isLayoutSizeAtLeast(SCREENLAYOUT_SIZE_LARGE);
        setUpToolbar(screenTitle);
        setUnBinder(ButterKnife.bind(getActivity()));
        dynamicReportPresenter.fetchData(menuCode, retailerId);

        setHasOptionsMenu(true);
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        this.menu = menu;
        inflater.inflate(R.menu.menu_dynamic_report, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        updateMenuIcon();
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.menu_freeze_columns) {
            if (adapter != null) {
                mCurrentFragment = (DynamicReportTabFragment) adapter.getRegisteredFragment(viewPager.getCurrentItem());
                showColumnFreezeDialog();
            }
            return true;
        } else if (i == R.id.menu_columns_hide) {
            if (adapter != null) {
                DynamicReportTabFragment mCurrentFragment = (DynamicReportTabFragment) adapter.getRegisteredFragment(viewPager.getCurrentItem());
                ColumnsHideDialog dialogFragment = new ColumnsHideDialog(getActivity(), getColumnList(false), mCurrentFragment);
                dialogFragment.show();
            }
            return true;
        } else if (i == R.id.menu_search_columns) {
            if (adapter != null) {
                ArrayList<DynamicReportBO> columnList = getColumnList(true);
                DynamicReportTabFragment mCurrentFragment = (DynamicReportTabFragment) adapter.getRegisteredFragment(viewPager.getCurrentItem());
                ColumnSearchDialog dialogFragment = new ColumnSearchDialog(getActivity(), columnList, mCurrentFragment, fetchSearchData(columnList), this);
                dialogFragment.show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setReportData(HashMap<String, HashMap<String, DynamicReportBO>> fieldList, HashMap<String, HashMap<String, HashMap<String, String>>> dataMap, ArrayList<String> headerList) {
        this.fieldsMap = fieldList;
        this.headerList = headerList;
        createTabs(fieldList, dataMap, headerList);
    }

    private void createTabs(HashMap<String, HashMap<String, DynamicReportBO>> fieldList, HashMap<String, HashMap<String, HashMap<String, String>>> dataMap, ArrayList<String> headerList) {
        for (String header : headerList) {
            tab.addTab(tab.newTab().setText(header));
        }
        tab.setTabGravity(TabLayout.GRAVITY_FILL);
        if (!is7InchTablet && tab.getTabCount() > 3) {
            tab.setTabMode(TabLayout.MODE_SCROLLABLE);
        }
        adapter = new DynamicReportPagerAdapter(getFragmentManager(), tab.getTabCount(), fieldList, dataMap, headerList);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(tab.getTabCount());
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tab));

        tab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                isClearMenu = fetchSearchData(getColumnList(true)) == null;
                updateMenuIcon();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    @Override
    public void showDataNotMappedMsg() {
        showMessage(R.string.data_not_mapped);
    }

    private ActionBar getActionBar() {
        return ((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar();
    }

    private void setUpActionBar() {
        if (getActionBar() != null) {
            getActionBar().setDisplayShowTitleEnabled(false);
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getActionBar().setElevation(0);
            }

            if (screenTitle != null)
                setScreenTitle(screenTitle);

            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void setLocationAdapter() {
        columnAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.select_dialog_singlechoice) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                CheckedTextView view = (CheckedTextView) super.getView(position, convertView, parent);
                view.setText(getItem(position));
                return view;
            }
        };
    }

    private void showColumnFreezeDialog() {
        AlertDialog.Builder builder;

        builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.freeze_columns_alert_title));
        builder.setSingleChoiceItems(columnAdapter, selectedColumnCount,
                onColumnDialogClickListener);

        applyAlertDialogTheme(getActivity(), builder);
    }

    private DialogInterface.OnClickListener onColumnDialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int item) {
            mCurrentFragment.onScrollFreeze(Integer.valueOf(columnAdapter.getItem(item)));
            selectedColumnCount = item;

            dialog.dismiss();

        }
    };

    private ArrayList<DynamicReportBO> getColumnList(boolean isColumnSearch) {
        ArrayList<DynamicReportBO> dynamicReportList = new ArrayList<>();
        HashMap<String, DynamicReportBO> columnMap = fieldsMap.get(headerList.get(tab.getSelectedTabPosition()));
        for (String key : columnMap.keySet()) {
            DynamicReportBO reportBO = columnMap.get(key);
            if (!isColumnSearch) {
                dynamicReportList.add(reportBO);
            } else if (!reportBO.isSelected()) {
                dynamicReportList.add(reportBO);
            }
        }

        return dynamicReportList;
    }


    private DynamicReportBO fetchSearchData(ArrayList<DynamicReportBO> columnList) {
        for (int i = 0; i < columnList.size(); i++) {
            DynamicReportBO reportBO = columnList.get(i);
            if (reportBO.isSearched()) {
                return reportBO;
            }

        }
        return null;
    }

    @Override
    public void changeMenuTint(boolean isClear) {
        isClearMenu = isClear;
        updateMenuIcon();
    }

    public void updateMenuIcon() {

        MenuItem searchMenu = menu.findItem(R.id.menu_search_columns);
        Drawable drawable = searchMenu.getIcon();
        if (drawable != null) {
            drawable.mutate();
            if (!isClearMenu) {
                TypedValue typedValue = new TypedValue();
                Resources.Theme theme = getActivity().getTheme();
                theme.resolveAttribute(R.attr.accentcolor, typedValue, true);
                int color = typedValue.data;
                drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            } else {
                drawable.setColorFilter(null);
            }
        }
    }
}
