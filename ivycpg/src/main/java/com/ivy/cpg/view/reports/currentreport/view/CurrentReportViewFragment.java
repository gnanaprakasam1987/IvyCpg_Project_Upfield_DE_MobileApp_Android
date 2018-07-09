package com.ivy.cpg.view.reports.currentreport.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.base.view.BaseFragment;
import com.ivy.cpg.view.reports.currentreport.CurrentReportViewAdapter;
import com.ivy.cpg.view.reports.currentreport.ICurrentReportContract;
import com.ivy.cpg.view.reports.currentreport.di.CurrentReportModule;
import com.ivy.cpg.view.reports.currentreport.di.DaggerCurrentReportComponent;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ChildLevelBo;
import com.ivy.sd.png.bo.StockReportBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.LabelsMasterHelper;
import com.ivy.sd.png.provider.UserMasterHelper;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;
import java.util.Vector;

import javax.inject.Inject;

import butterknife.BindView;


public class CurrentReportViewFragment extends BaseFragment implements ICurrentReportContract.ICurrentReportView,
        CurrentReportViewAdapter.CurrentReportViewAdapterCallback,
        Spinner.OnItemSelectedListener {

    @BindView(R.id.list)
    ListView lvWpList;

    @BindView(R.id.productName)
    TextView productName;

    @BindView(R.id.brandSpinner)
    Spinner spinnerBrand;

    @BindView(R.id.sihCaseTitle)
    TextView sihCaseTitle;

    @BindView(R.id.sihOuterTitle)
    TextView sihOuterTitle;

    @BindView(R.id.sihTitle)
    TextView sihTitle;


    @Inject
    LabelsMasterHelper labelsMasterHelper;

    @Inject
    UserMasterHelper userMasterHelper;


    @Inject
    ICurrentReportContract.ICurrentReportModelPresenter<ICurrentReportContract.ICurrentReportView> currentReportModelPresenter;

    private Vector<StockReportBO> stockReportBOSList;

    @Override
    protected int setContentViewLayout() {
        return R.layout.fragment_stock_report;
    }

    @Override
    public void initVariables(View view) {
        initializeBusinessModel();
        spinnerBrand.setOnItemSelectedListener(this);
    }

    @Override
    protected void getMessageFromAliens() {
    }

    @Inject
    public void initializeUserHelper(UserMasterHelper userMasterHelper) {
        currentReportModelPresenter.setUserMasterHelper(userMasterHelper);
    }

    @Inject
    public void initializeLabelMaster(LabelsMasterHelper labelsMasterHelper) {
        currentReportModelPresenter.setLabelsMasterHelper(labelsMasterHelper);
    }

    @Override
    protected void setUpViews() {

        currentReportModelPresenter.setSihTitle(sihTitle.getTag());

        currentReportModelPresenter.checkUserId();

        currentReportModelPresenter.setUpTitles();

        lvWpList.setCacheColorHint(0);

        setUpSpinner();

    }


    private void setUpSpinner() {

        BusinessModel bModel = (BusinessModel) getActivity().getApplicationContext();
        bModel.setContext(getActivity());

        Vector<ChildLevelBo> items = new Vector<>();
        try {
            items = bModel.productHelper.getChildLevelBo();
            if (items.size() == 0)
                return;
        } catch (Exception e) {
            Commons.printException(e);
        }


        currentReportModelPresenter.downloadCurrentStockReport(getActivity(), bModel);

        bModel.reportHelper.updateBaseUOM("ORDER", 2);

        ArrayAdapter<ChildLevelBo> childAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item);
        childAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        childAdapter.add(new ChildLevelBo(0, 0, getResources().getString(R.string.all)));

        for (int i = 0; i < items.size(); ++i) {
            childAdapter.add(items.elementAt(i));
        }
        spinnerBrand.setAdapter(childAdapter);
    }


    private void initializeBusinessModel() {

    }


    @Override
    public void initializeDi() {
        DaggerCurrentReportComponent.builder().currentReportModule(new CurrentReportModule(this, getActivity().getApplication()))
                .ivyAppComponent(((BusinessModel) getActivity().getApplication()).getComponent()).
                build().inject(this);
        setBasePresenter((BasePresenter) currentReportModelPresenter);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void showNoProductError() {
        showAlert("IvyCpg", getString(R.string.no_products_exists));
    }


    @Override
    public void productName(String pName) {
        productName.setText(pName);
    }

    @Override
    public void setAdapter(ArrayList<StockReportBO> myList, ConfigurationMasterHelper configurationMasterHelper) {
        CurrentReportViewAdapter mSchedule = new CurrentReportViewAdapter(myList, getActivity(), configurationMasterHelper, CurrentReportViewFragment.this);
        lvWpList.setAdapter(mSchedule);
    }

    @Override
    public void setUpViewsVisible() {
        sihCaseTitle.setVisibility(View.VISIBLE);
        sihOuterTitle.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideTitleViews() {
        sihCaseTitle.setVisibility(View.GONE);
        sihOuterTitle.setVisibility(View.GONE);
    }

    @Override
    public void setSihTitle(String title) {
        sihTitle.setText(title);
    }

    @Override
    public void finishActivity() {
        showMessage(getString(R.string.sessionout_loginagain));
        getActivity().finish();
    }

    @Override
    public void showError() {

    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        ChildLevelBo temp = (ChildLevelBo) parent.getSelectedItem();

        currentReportModelPresenter.updateStockReportGrid(temp.getProductid(), stockReportBOSList);
        productName.setText("");
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void setStockReportBOSList(Vector<StockReportBO> stockReportBOSList) {
        this.stockReportBOSList = stockReportBOSList;
    }
}
