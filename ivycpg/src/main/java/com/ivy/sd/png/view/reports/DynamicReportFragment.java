package com.ivy.sd.png.view.reports;


import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.DynamicReportDetailBO;
import com.ivy.sd.png.bo.DynamicReportHeaderBO;
import com.ivy.sd.png.bo.SpinnerBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.ReportUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

/**
 * A simple {@link Fragment} subclass.
 */
public class DynamicReportFragment extends Fragment {


    private BusinessModel bmodel;
    private View view;
    private List<DynamicReportHeaderBO> headers;
    private DynamicReportDetailBO details;
    private LinearLayout headerLayout, detailsLayout, ll_item, ll_report_retailer;
    private Spinner retailerSpinner;
    private ArrayAdapter<SpinnerBO> reportArrayAdapter;


    public DynamicReportFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        String isFrom = getArguments().getString("isFrom");
        view = inflater.inflate(R.layout.fragment_dynamic_report, container,
                false);


        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

        headerLayout = (LinearLayout) view.findViewById(R.id.header);
        detailsLayout = (LinearLayout) view.findViewById(R.id.detailsView);
        //Spinner for retailer type report
        ll_report_retailer = (LinearLayout) view.findViewById(R.id.ll_report_retailer);
        retailerSpinner = (Spinner) view.findViewById(R.id.retailerSpinner);

        //to change background color for list item if it is expense
        ll_item = (LinearLayout) view.findViewById(R.id.ll_item);

        if (isFrom.equals("Expense"))
            ll_item.setBackgroundResource(R.color.WHITE);
        else
            ll_item.setBackgroundResource(R.drawable.background_item);

        if (bmodel.dynamicReportHelper.isRep_retailer()) {
            ll_report_retailer.setVisibility(View.VISIBLE);
            reportArrayAdapter = new ArrayAdapter<SpinnerBO>(getActivity(), android.R.layout.simple_spinner_item);
            reportArrayAdapter.add(new SpinnerBO(0, getResources().getString(R.string.select)));
            if (bmodel.dynamicReportHelper.getReportRetailer() != null &&
                    bmodel.dynamicReportHelper.getReportRetailer().size() != 0) {
                for (SpinnerBO bo : bmodel.dynamicReportHelper.getReportRetailer())
                    reportArrayAdapter.add(bo);
            }
            retailerSpinner.setAdapter(reportArrayAdapter);
        } else
            ll_report_retailer.setVisibility(View.GONE);

        setUpReportLayout();

        retailerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                int selectedId = reportArrayAdapter.getItem(position).getId();
                if (selectedId > 0) {
                    bmodel.dynamicReportHelper.downloadRetailerReport(reportArrayAdapter.getItem(position).getId());
                    setUpReportLayout();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        return view;
    }

    private SparseArray<SparseArray<String>> detailsSparseArray;
    private TreeSet<Integer> recordSet;

    private void setUpReportLayout() {
        headers = new ArrayList<DynamicReportHeaderBO>();
        details = new DynamicReportDetailBO();
        detailsSparseArray = new SparseArray<>();
        recordSet = new TreeSet<>();

        headers = bmodel.dynamicReportHelper.getDynamicReportHeaderBOs();
        details = bmodel.dynamicReportHelper.getDynamicReportDetailBO();

        detailsSparseArray = details.getDetailsSparseArray();
        recordSet = details.getRecordSet();

        if (headerLayout != null)
            headerLayout.removeAllViews();
        if (detailsLayout != null)
            detailsLayout.removeAllViews();

        try {

            int rowCount = recordSet.size();
            Iterator<Integer> it = recordSet.iterator();
            boolean firstIteration = true;
            while (it.hasNext()) {
                int recordId = it.next();
                LinearLayout linearLayout = new LinearLayout(getActivity());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ReportUtil.dpToPixel(getActivity(), 45));
                linearLayout.setLayoutParams(params);
                linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                linearLayout.setBackgroundResource(R.drawable.list_selector);

                int headerSize = headers.size();
                for (int j = 0; j < headerSize; j++) {
                    DynamicReportHeaderBO dynamicReportHeaderBO = headers.get(j);

                    int length = (dynamicReportHeaderBO.getLength() > 0) ? dynamicReportHeaderBO.getLength() : 0;
                    int pixelLength = ReportUtil.dpToPixel(getActivity(), length);
                    String value = (dynamicReportHeaderBO.getColumnName() != null) ? dynamicReportHeaderBO.getColumnName() : "";
                    int textSizeSmall = (int) (getResources().getDimension(R.dimen.font_small) / getResources().getDisplayMetrics().density);
                    int textSizeMedium = (int) (getResources().getDimension(R.dimen.font_medium) / getResources().getDisplayMetrics().density);


                    if (firstIteration) {
                        TextView textView = new TextView(getActivity(), null, R.style.TextViewListTitle);
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeSmall);
                        textView.setBackgroundResource(R.drawable.list_title_bg);
                        textView.setTextColor(Color.WHITE);
                        textView.setGravity(Gravity.CENTER);
                        textView.setTypeface(null, Typeface.BOLD);
                        textView.setLayoutParams(new LinearLayout.LayoutParams(pixelLength, ViewGroup.LayoutParams.MATCH_PARENT));
                        int padding = (int) getResources().getDimension(R.dimen.list_title_padding);
                        textView.setPadding(padding, padding, padding, padding);
                        textView.setText(value);
                        headerLayout.addView(textView);
                    }

                    TextView textViewF = new TextView(getActivity(), null, R.style.TextViewListTitle);
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        textViewF.setId(ReportUtil.generateViewId());
                    } else {
                        textViewF.setId(View.generateViewId());
                    }
                    textViewF.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeSmall);
                    textViewF.setLayoutParams(new LinearLayout.LayoutParams(pixelLength, ViewGroup.LayoutParams.MATCH_PARENT));
                    textViewF.setTextColor(Color.BLACK);
                    textViewF.setGravity(Gravity.CENTER);
                    SparseArray<String> list = null;
                    String data = null;
                    try {
                        list = detailsSparseArray.get(dynamicReportHeaderBO.getColumnId());
                        data = list.get(recordId);
                    } catch (Exception e) {

                    }

                    if (data != null) {
                        textViewF.setText(data);
                    }

                    linearLayout.addView(textViewF);
                }

                detailsLayout.addView(linearLayout);

                View dividerView = new View(getActivity());
                dividerView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, ReportUtil.dpToPixel(getActivity(), 1)));
                dividerView.setBackgroundColor(Color.rgb(51, 51, 51));
                detailsLayout.addView(dividerView);

                firstIteration = false;
            }
        } catch (Exception e) {
            Commons.printException(e);
        }

    }

}
