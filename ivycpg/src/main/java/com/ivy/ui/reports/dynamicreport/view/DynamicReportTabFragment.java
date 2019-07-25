package com.ivy.ui.reports.dynamicreport.view;

import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
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
import com.ivy.ui.reports.dynamicreport.model.DynamicReportBO;
import com.ivy.utils.DeviceUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import androidx.core.graphics.ColorUtils;
import butterknife.BindView;

public class DynamicReportTabFragment extends BaseFragment implements BaseIvyView, DynamicReportFragmentNew.DialogListener {

    private String mSelectedTab;
    private HashMap<String, HashMap<String, HashMap<String, String>>> dataMap;
    private HashMap<String, HashMap<String, DynamicReportBO>> fieldsMap;

    @Inject
    BaseIvyPresenter<BaseIvyView> viewBasePresenter;

    @BindView(R.id.scrollable_part)
    TableLayout tableScrollableRows;

    @BindView(R.id.fixed_column)
    TableLayout tableFixedColumns;

    private int freezeColumns;

    public static DynamicReportTabFragment newInstance(String mSelectedTab, HashMap<String, HashMap<String, HashMap<String, String>>> dataMap,
                                                       HashMap<String, HashMap<String, DynamicReportBO>> fieldsMap) {

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
            fieldsMap = (HashMap<String, HashMap<String, DynamicReportBO>>) getArguments().getSerializable("fields");
        }
    }

    @Override
    protected void setUpViews() {
        buildTable(dataMap);
    }

    private void buildTable(final HashMap<String, HashMap<String, HashMap<String, String>>> valueMap) {

        tableScrollableRows.removeAllViews();
        tableFixedColumns.removeAllViews();

        // add table header row
        final TableRow tr = new TableRow(getActivity());

        final TableRow fixedHeader = new TableRow(getActivity());

        int count = 0;
        int padding = (int) getResources().getDimension(R.dimen.dimens_20dp);
        for (String displayname : fieldsMap.get(mSelectedTab).keySet()) {
            DynamicReportBO dynamicReportBO = fieldsMap.get(mSelectedTab).get(displayname);
            if (!dynamicReportBO.isSelected()) {
                int pixelLength = DeviceUtils.dpToPixel(getActivity(), dynamicReportBO.getLength());
                TextView textHeader = (TextView) getActivity().getLayoutInflater().inflate(R.layout.dynamic_report_table_header, null);
                textHeader.setLayoutParams(new TableRow.LayoutParams(pixelLength,
                        TableLayout.LayoutParams.WRAP_CONTENT));

                int alignment = Gravity.CENTER;
                if ("Left".equalsIgnoreCase(dynamicReportBO.getAlign()))
                    alignment = Gravity.START;
                else if ("Right".equalsIgnoreCase(dynamicReportBO.getAlign()))
                    alignment = Gravity.END;

                textHeader.setGravity(alignment | Gravity.CENTER_VERTICAL);

                textHeader.setMaxLines(1);
                textHeader.setEllipsize(TextUtils.TruncateAt.END);
                textHeader.setMinHeight((int) getResources().getDimension(R.dimen.height_list_row));
                textHeader.setText(fieldsMap.get(mSelectedTab).get(displayname).getDisplayName());
                textHeader.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dynamicReportBO.setSorted(true);
                        changeSortedColumnColour(dynamicReportBO.getFieldName());
                        sortMap(valueMap, displayname);
                    }
                });

                if (count < freezeColumns)
                    fixedHeader.addView(textHeader);
                else
                    tr.addView(textHeader);

                count++;
            }

        }

        tableFixedColumns.addView(fixedHeader);
        tableScrollableRows.addView(tr);


        for (int i = 0; i < valueMap.get(mSelectedTab).size(); i++) {

            // add table values row
            final TableRow row = new TableRow(getActivity());

            final TableRow fixedRow = new TableRow(getActivity());

            count = 0;
            for (String fieldname : fieldsMap.get(mSelectedTab).keySet()) {
                DynamicReportBO dynamicReportBO = fieldsMap.get(mSelectedTab).get(fieldname);
                if (!dynamicReportBO.isSelected()) {
                    int pixelLength = DeviceUtils.dpToPixel(getActivity(), dynamicReportBO.getLength());
                    TextView textChild = (TextView) getActivity().getLayoutInflater().inflate(R.layout.dynamic_report_table_row_child, null);
                    textChild.setLayoutParams(new TableRow.LayoutParams(pixelLength,
                            TableLayout.LayoutParams.WRAP_CONTENT));

                    int alignment = Gravity.CENTER;
                    if ("Left".equalsIgnoreCase(dynamicReportBO.getAlign()))
                        alignment = Gravity.START;
                    else if ("Right".equalsIgnoreCase(dynamicReportBO.getAlign()))
                        alignment = Gravity.END;

                    textChild.setGravity(alignment | Gravity.CENTER_VERTICAL);

                    textChild.setMaxLines(1);
                    textChild.setEllipsize(TextUtils.TruncateAt.END);
                    textChild.setMinHeight((int) getResources().getDimension(R.dimen.height_list_row));
                    textChild.setPadding(0, 0, padding, 0);
                    textChild.setText(getFieldValue(valueMap, fieldname, String.valueOf(i)));
                    if (dynamicReportBO.isSorted()) {
                        TypedValue typedValue = new TypedValue();
                        Resources.Theme theme = getActivity().getTheme();
                        theme.resolveAttribute(R.attr.colorPrimary, typedValue, true);
                        textChild.setBackgroundColor(ColorUtils.setAlphaComponent(typedValue.data,25));
                    }
                    if (count < freezeColumns)
                        fixedRow.addView(textChild);
                    else
                        row.addView(textChild);

                    count++;
                }
            }

            tableFixedColumns.addView(fixedRow);
            tableScrollableRows.addView(row);
        }
    }

    private String getFieldValue(HashMap<String, HashMap<String, HashMap<String, String>>> dataMap, String fieldname, String key) {

        HashMap<String, String> valueMap = dataMap.get(mSelectedTab).get(key);
        for (String name : dataMap.get(mSelectedTab).get(key).keySet()) {
            if (fieldname.equals(name))
                return valueMap.get(name);
        }
        return null;
    }

    private void sortMap(HashMap<String, HashMap<String, HashMap<String, String>>> dataMap, String fieldname) {

        ArrayList<String> valueList = new ArrayList<>();
        for (int i = 0; i < dataMap.get(mSelectedTab).size(); i++) {
            valueList.add(getFieldValue(dataMap, fieldname, String.valueOf(i)) + "_" + i);
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

        buildTable(dataMap);

    }

    private void searchText() {
        String fieldname = "";
        String searchValue = "";
        for (String displayname : fieldsMap.get(mSelectedTab).keySet()) {
            DynamicReportBO dynamicReportBO = fieldsMap.get(mSelectedTab).get(displayname);
            if (dynamicReportBO.isSearched()) {
                fieldname = displayname;
                searchValue = dynamicReportBO.getSearchText();
            }
        }

        ArrayList<String> valueList = new ArrayList<>();
        for (int i = 0; i < dataMap.get(mSelectedTab).size(); i++) {
            String value = getFieldValue(dataMap, fieldname, String.valueOf(i));
            if (searchValue.equalsIgnoreCase(value))
                valueList.add(String.valueOf(i));
        }

        HashMap<String, HashMap<String, HashMap<String, String>>> searchedMap = new HashMap<>();
        searchedMap.putAll(dataMap);
        HashMap<String, HashMap<String, String>> sortedMap = new HashMap<>();
        int key = 0;
        for (String value : valueList) {
            sortedMap.put(String.valueOf(key), dataMap.get(mSelectedTab).get(value));
            key++;
        }

        searchedMap.put(mSelectedTab, sortedMap);
        buildTable(searchedMap);
    }

    public void onScrollFreeze(int freezeCount) {
        freezeColumns = freezeCount;
        buildTable(dataMap);
    }

    @Override
    public void onColumnHide() {
        buildTable(dataMap);
    }

    @Override
    public void onColumnSearch(boolean isClear) {
        if (isClear)
            buildTable(dataMap);
        else
            searchText();

    }

    private void changeSortedColumnColour(String field) {
        for (String displayname : fieldsMap.get(mSelectedTab).keySet()) {
            DynamicReportBO dynamicReportBO = fieldsMap.get(mSelectedTab).get(displayname);
            if (!field.equals(dynamicReportBO.getFieldName()))
                dynamicReportBO.setSorted(false);
        }
    }
}
