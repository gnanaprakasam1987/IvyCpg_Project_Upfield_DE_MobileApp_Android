package com.ivy.ui.reports.dynamicreport.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
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

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.res.Configuration.SCREENLAYOUT_SIZE_LARGE;

public class DynamicReportFragmentNew extends BaseFragment implements DynamicReportContract.DynamicReportView {

    private String screenTitle;
    private String menuCode = "";
    private String retailerId = "0";
    private boolean is7InchTablet;

    @Inject
    DynamicReportContract.DynamicReportPresenter<DynamicReportContract.DynamicReportView> dynamicReportPresenter;

    @BindView(R.id.tabs)
    TabLayout tab;

    @BindView(R.id.viewPager)
    ViewPager viewPager;

    ArrayAdapter<String> columnAdapter;

    int selectedColumnCount = 0;

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
        for (int i = 1; i <= 2; i++) {
            columnAdapter.add(String.valueOf(i));
        }
    }

    @Override
    protected void getMessageFromAliens() {
        if (getArguments() != null) {
            screenTitle = getArguments().getString("screentitle");
            menuCode = getArguments().getString("menucode");
            retailerId = getArguments().getString("rid", "0");
        }
    }

    @Override
    protected void setUpViews() {
        setUpActionBar();
        is7InchTablet = getResources().getConfiguration().isLayoutSizeAtLeast(SCREENLAYOUT_SIZE_LARGE);
        setUpToolbar(screenTitle);
        setHasOptionsMenu(true);
        setUnBinder(ButterKnife.bind(getActivity()));
        dynamicReportPresenter.fetchData(menuCode, retailerId);

        setHasOptionsMenu(true);
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void setReportData(HashMap<String, HashMap<String, String>> fieldList, HashMap<String, HashMap<String, HashMap<String, String>>> dataMap, ArrayList<String> headerList) {
        createTabs(fieldList, dataMap, headerList);
    }

    private void createTabs(HashMap<String, HashMap<String, String>> fieldList, HashMap<String, HashMap<String, HashMap<String, String>>> dataMap, ArrayList<String> headerList) {
        for (String header : headerList) {
            tab.addTab(tab.newTab().setText(header));
        }
        tab.setTabGravity(TabLayout.GRAVITY_FILL);
        if (!is7InchTablet && tab.getTabCount() > 3) {
            tab.setTabMode(TabLayout.MODE_SCROLLABLE);
        }
        DynamicReportPagerAdapter adapter = new DynamicReportPagerAdapter(getFragmentManager(), tab.getTabCount(), fieldList, dataMap, headerList);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(tab.getTabCount());
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tab));

        tab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
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
        getActionBar().setDisplayShowTitleEnabled(false);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActionBar().setElevation(0);
        }

        if (screenTitle != null)
            setScreenTitle(screenTitle);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_dynamic_report, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.menu_freeze_columns) {
            showColumnFreezeDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
            EventBus.getDefault().post(columnAdapter
                    .getItem(item));
            selectedColumnCount = item;

            dialog.dismiss();

        }
    };
}
