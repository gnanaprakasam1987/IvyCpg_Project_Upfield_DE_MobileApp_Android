package com.ivy.ui.reports.dynamicreport.view;

import android.os.Bundle;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.base.view.BaseFragment;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.cpg.view.basedi.BaseModule;
import com.ivy.cpg.view.basedi.DaggerBaseComponent;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import butterknife.BindView;

public class DynamicReportTabFragment extends BaseFragment implements BaseIvyView {

    private String mSelectedTab;
    private HashMap<String, HashMap<String, HashMap<String, String>>> dataMap;
    private HashMap<String, HashMap<String, String>> fieldsMap;

    @Inject
    BaseIvyPresenter<BaseIvyView> viewBasePresenter;

    @BindView(R.id.scrollable_part)
    TableLayout tableScrollableRows;

    @BindView(R.id.fixed_column)
    TableLayout tableFixedColumns;

    private int freezeColumns;

    public static DynamicReportTabFragment newInstance(String mSelectedTab, HashMap<String, HashMap<String, HashMap<String, String>>> dataMap,
                                                       HashMap<String, HashMap<String, String>> fieldsMap) {

        DynamicReportTabFragment tabFragment = new DynamicReportTabFragment();
        Bundle args = new Bundle();
        args.putString("tab", mSelectedTab);
        args.putSerializable("fields", fieldsMap);
        args.putSerializable("values", dataMap);
        tabFragment.setArguments(args);
        return tabFragment;
    }

    @Override
    public void initializeDi() {
        DaggerBaseComponent.builder()
                .baseModule(new BaseModule(this))
                .ivyAppComponent(((BusinessModel) getActivity().getApplicationContext()).getComponent())
                .build()
                .inject(this);
        setBasePresenter((BasePresenter) viewBasePresenter);
    }

    @Override
    protected int setContentViewLayout() {
        return R.layout.dynamic_report_fragment_tab;
    }

    @Override
    public void init(View view) {

    }

    @Override
    protected void getMessageFromAliens() {
        if (getArguments() != null) {
            mSelectedTab = getArguments().getString("tab");
            dataMap = (HashMap<String, HashMap<String, HashMap<String, String>>>) getArguments().getSerializable("values");
            fieldsMap = (HashMap<String, HashMap<String, String>>) getArguments().getSerializable("fields");
        }
    }

    @Override
    protected void setUpViews() {
        buildTable();
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
    public void onMessageEvent(String columns) {
        freezeColumns = Integer.valueOf(columns);
        buildTable();
    }

    private void buildTable() {

        tableScrollableRows.removeAllViews();
        tableFixedColumns.removeAllViews();

        // add table header row
        final TableRow tr = new TableRow(getActivity());

        final TableRow fixedHeader = new TableRow(getActivity());

        int count = 0;
        for (String displayname : fieldsMap.get(mSelectedTab).keySet()) {
            TextView textHeader = (TextView) getActivity().getLayoutInflater().inflate(R.layout.dynamic_report_table_header, null);
            textHeader.setLayoutParams(new TableRow.LayoutParams((int) getResources().getDimension(R.dimen.filter_level_list_width),
                    TableLayout.LayoutParams.WRAP_CONTENT, 1f));

            textHeader.setMinHeight((int) getResources().getDimension(R.dimen.height_list_row));
            textHeader.setText(fieldsMap.get(mSelectedTab).get(displayname));
            textHeader.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sortMap(displayname);
                }
            });

            if (count < freezeColumns)
                fixedHeader.addView(textHeader);
            else
                tr.addView(textHeader);

            count++;

        }

        tableFixedColumns.addView(fixedHeader);
        tableScrollableRows.addView(tr);


        for (int i = 0; i < dataMap.get(mSelectedTab).size(); i++) {

            // add table values row
            final TableRow row = new TableRow(getActivity());

            final TableRow fixedRow = new TableRow(getActivity());

            count = 0;
            for (String fieldname : fieldsMap.get(mSelectedTab).keySet()) {
                TextView textChild = (TextView) getActivity().getLayoutInflater().inflate(R.layout.dynamic_report_table_row_child, null);
                textChild.setLayoutParams(new TableRow.LayoutParams((int) getResources().getDimension(R.dimen.filter_level_list_width),
                        TableLayout.LayoutParams.WRAP_CONTENT, 1f));
                textChild.setMinHeight((int) getResources().getDimension(R.dimen.height_list_row));
                textChild.setText(getFieldValue(fieldname, String.valueOf(i)));
                if (count < freezeColumns)
                    fixedRow.addView(textChild);
                else
                    row.addView(textChild);

                count++;
            }

            tableFixedColumns.addView(fixedRow);
            tableScrollableRows.addView(row);
        }
    }

    private String getFieldValue(String fieldname, String key) {

        HashMap<String, String> valueMap = dataMap.get(mSelectedTab).get(key);
        for (String name : dataMap.get(mSelectedTab).get(key).keySet()) {
            if (fieldname.equals(name))
                return valueMap.get(name);
        }
        return null;
    }

    private void sortMap(String fieldname) {

        ArrayList<String> valueList = new ArrayList<>();
        for (int i = 0; i < dataMap.get(mSelectedTab).size(); i++) {
            valueList.add(getFieldValue(fieldname, String.valueOf(i)) + "_" + i);
        }

        final Pattern p = Pattern.compile("^\\d+");
        Comparator<String> c = new Comparator<String>() {
            @Override
            public int compare(String object1, String object2) {
                Matcher m = p.matcher(object1);
                Integer number1;
                if (!m.find()) {
                    return object1.compareTo(object2);
                } else {
                    int number2;
                    number1 = Integer.parseInt(m.group());
                    m = p.matcher(object2);
                    if (!m.find()) {
                        return object1.compareTo(object2);
                    } else {
                        number2 = Integer.parseInt(m.group());
                        int comparison = number1.compareTo(number2);
                        if (comparison != 0) {
                            return comparison;
                        } else {
                            return object1.compareTo(object2);
                        }
                    }
                }
            }
        };
        Collections.sort(valueList, c);

        HashMap<String, HashMap<String, String>> sortedMap = new HashMap<>();
        int key = 0;
        for (String value : valueList) {
            sortedMap.put(String.valueOf(key), dataMap.get(mSelectedTab).get(value.split("_")[1]));
            key++;
        }

        dataMap.put(mSelectedTab, sortedMap);

        buildTable();

    }

}
