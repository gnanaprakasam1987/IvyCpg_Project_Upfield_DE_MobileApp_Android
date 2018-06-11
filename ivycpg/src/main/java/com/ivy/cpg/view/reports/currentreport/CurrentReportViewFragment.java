package com.ivy.cpg.view.reports.currentreport;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ChildLevelBo;
import com.ivy.sd.png.bo.StockReportBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

import java.util.Vector;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class CurrentReportViewFragment extends Fragment implements ICurrentReportView {

    private ICurrentReportModelPresenter currentReportModelPresenter = null;
    private BusinessModel bModel;
    private Unbinder unbinder;

    @BindView(R.id.list)
    ListView lvWpList;

    @BindView(R.id.productName)
    TextView productName;

    @BindView(R.id.brandSpinner)
    Spinner spinnerBrand;

    private void initializeModel() {
        currentReportModelPresenter = new CurrentReportModel(getActivity(), bModel, CurrentReportViewFragment.this);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeBusinessModel();
        initializeModel();
    }

    private void initializeBusinessModel() {
        bModel = (BusinessModel) getActivity().getApplicationContext();
        bModel.setContext(getActivity());

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        View view = inflater.inflate(R.layout.fragment_stock_report, container,
                false);
        unbinder = ButterKnife.bind(this, view);

        initializeBusinessModel();

        try {
            if (bModel.labelsMasterHelper.applyLabels(view.findViewById(R.id.sihtitle).getTag()) != null)
                ((TextView) view.findViewById(R.id.sihtitle)).setText(bModel.labelsMasterHelper.applyLabels(view
                        .findViewById(R.id.sihtitle).getTag()));
        } catch (Exception e1) {
            Commons.printException("" + e1);
        }

        if (bModel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(), getResources().getString(R.string.sessionout_loginagain), Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

        if (bModel.configurationMasterHelper.IS_EOD_STOCK_SPLIT) {
            view.findViewById(R.id.sihCaseTitle).setVisibility(View.VISIBLE);
            view.findViewById(R.id.sihOuterTitle).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.sihCaseTitle).setVisibility(View.GONE);
            view.findViewById(R.id.sihOuterTitle).setVisibility(View.GONE);
        }


        productName.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                int inType = productName.getInputType();
                productName.setInputType(InputType.TYPE_NULL);
                productName.onTouchEvent(event);
                productName.setInputType(inType);
//                productName.performClick();
                return true;
            }


        });


        lvWpList.setCacheColorHint(0);

        Vector<ChildLevelBo> items = new Vector<>();
        int siz = 0;
        try {
            items = bModel.productHelper.getChildLevelBo();
            siz = items.size();
            if (siz == 0)
                return view;
        } catch (Exception e) {
            Commons.printException(e);
        }


        final Vector<StockReportBO> mylist = bModel.reportHelper.downloadCurrentStockReport();
        bModel.reportHelper.updateBaseUOM("ORDER", 2);

        ArrayAdapter<ChildLevelBo> childAdapter = new ArrayAdapter<>(
                getActivity(), android.R.layout.simple_spinner_item);
        childAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        childAdapter.add(new ChildLevelBo(0, 0, getResources().getString(
                R.string.all)));
        for (int i = 0; i < siz; ++i) {
            childAdapter.add(items.elementAt(i));
        }
        spinnerBrand.setAdapter(childAdapter);
        spinnerBrand.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                ChildLevelBo temp = (ChildLevelBo) parent.getSelectedItem();
                //  updateStockReportGrid(temp.getProductid(), mylist);

                currentReportModelPresenter.updateStockReportGrid(temp.getProductid(), mylist);
                productName.setText("");
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        return view;
    }

    @Override
    public void onDestroy() {
        unbinder.unbind();
        super.onDestroy();
    }

    @Override
    public void setAdapter(CurrentReportViewAdapter adapter) {
        lvWpList.setAdapter(adapter);
    }

    @Override
    public void setProductName(String pName) {
        productName.setText(pName);
    }

}
