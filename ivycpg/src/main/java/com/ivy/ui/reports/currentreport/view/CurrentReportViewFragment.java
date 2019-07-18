package com.ivy.ui.reports.currentreport.view;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.base.view.BaseFragment;
import com.ivy.sd.png.provider.ProductHelper;
import com.ivy.ui.reports.currentreport.CurrentReportViewAdapter;
import com.ivy.ui.reports.currentreport.ICurrentReportContract;
import com.ivy.ui.reports.currentreport.di.CurrentReportModule;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ChildLevelBo;
import com.ivy.sd.png.bo.StockReportBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.ui.reports.currentreport.di.DaggerCurrentReportComponent;


import java.util.ArrayList;
import java.util.Vector;

import javax.inject.Inject;
import javax.inject.Singleton;

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
    ProductHelper productHelper;

    @Inject
    ICurrentReportContract.ICurrentReportModelPresenter<ICurrentReportContract.ICurrentReportView> currentReportModelPresenter;

    private Vector<StockReportBO> stockReportBOSList;

    @Override
    protected int setContentViewLayout() {
        return R.layout.fragment_stock_report;
    }

    @Override
    public void init(View view) {
        spinnerBrand.setOnItemSelectedListener(this);
    }

    @Override
    protected void getMessageFromAliens() {}


    @Override
    protected void setUpViews() {
        currentReportModelPresenter.downLoadUserDetails();
        currentReportModelPresenter.setSihTitle(sihTitle.getTag());

        currentReportModelPresenter.checkUserId();

        currentReportModelPresenter.setUpTitles();
        lvWpList.setCacheColorHint(0);

        currentReportModelPresenter.getSpinnerData();
    }

    
    @Override
    public void initializeDi() {
        DaggerCurrentReportComponent.builder()
                .currentReportModule(new CurrentReportModule(this, getActivity().getApplication()))
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
    public void showError(String message) {
        showMessage(message);
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

    @Override
    public void setUpBrandSpinner(Vector<ChildLevelBo> items) {
        currentReportModelPresenter.updateBaseUOM(getActivity(), "ORDER", 2);

        ArrayAdapter<ChildLevelBo> childAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item);
        childAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        childAdapter.add(new ChildLevelBo(0, 0, getResources().getString(R.string.all)));

        for (int i = 0; i < items.size(); ++i) {
            childAdapter.add(items.elementAt(i));
        }
        spinnerBrand.setAdapter(childAdapter);
    }


}
