package com.ivy.ui.reports.beginstockreport.view;


import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.base.view.BaseFragment;
import com.ivy.ui.reports.beginstockreport.BeginningReportContract;
import com.ivy.ui.reports.beginstockreport.BeginningStockAdapter;

import com.ivy.ui.reports.beginstockreport.di.BeginningReportModule;
import com.ivy.ui.reports.beginstockreport.di.DaggerBeginningReportComponent;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.StockReportMasterBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.LabelsMasterHelper;
import com.ivy.sd.png.provider.UserMasterHelper;


import java.util.Vector;

import javax.inject.Inject;

import butterknife.BindView;

public class BeginningStockFragment extends BaseFragment implements
        BeginningReportContract.IBeginningStockView, BeginningStockAdapter.BeginningStockAdapterCallback {
    @BindView(R.id.list)
    ListView lvWpList;

    @BindView(R.id.productName)
    TextView productName;

    @BindView(R.id.caseTitle)
    TextView caseTitle;

    @BindView(R.id.pcsTitle)
    TextView pcsTitle;

    @BindView(R.id.totaltitle)
    TextView totalTitle;

    @BindView(R.id.outerTitle)
    TextView outerTitle;

    @Inject
    BeginningReportContract.IBeginningStockModelPresenter<BeginningReportContract.IBeginningStockView> beginningReportModelPresenter;


    @Override
    public void initializeDi() {
        DaggerBeginningReportComponent.builder().beginningReportModule(new BeginningReportModule(this, getActivity().getApplication()))
                .ivyAppComponent(((BusinessModel) getActivity().getApplication()).getComponent()).
                build().inject(this);

        setBasePresenter((BasePresenter) beginningReportModelPresenter);
    }


    @Inject
    public void initializeLabelMaster(LabelsMasterHelper labelsMasterHelper) {
        beginningReportModelPresenter.setLabelsMasterHelper(labelsMasterHelper);
    }

    @Inject
    public void initializeUserHelper(UserMasterHelper userMasterHelper) {
        beginningReportModelPresenter.setUserMasterHelper(userMasterHelper);
    }

    @Override
    protected int setContentViewLayout() {
        return R.layout.fragment_begining_stock;
    }

    @Override
    public void initVariables(View view) {

    }

    @Override
    protected void getMessageFromAliens() {

    }

    @Override
    protected void setUpViews() {

        //beginningReportModelPresenter.setCaseAndPieceTitle(caseTitle.getTag(), pcsTitle.getTag());

        beginningReportModelPresenter.checkUserId();
        lvWpList.setCacheColorHint(0);


       /* productName.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                int inType = productName.getInputType();
                productName.setInputType(InputType.TYPE_NULL);
                productName.onTouchEvent(event);
                productName.setInputType(inType);
                return true;
            }
        });*/


        beginningReportModelPresenter.downloadBeginningStock(getActivity());

        beginningReportModelPresenter.showCaseOrder(caseTitle.getTag());

        beginningReportModelPresenter.showPieceOrder(pcsTitle.getTag());

        beginningReportModelPresenter.showTotalTitle(totalTitle.getTag());

        if (!beginningReportModelPresenter.ifShowCase())
            outerTitle.setVisibility(View.GONE);
    }


    @Override
    public void productName(String pName) {
        productName.setText(pName);
    }

    @Override
    public void setAdapter(Vector<StockReportMasterBO> stockReportMasterBOS, ConfigurationMasterHelper configurationMasterHelper) {
        BeginningStockAdapter mSchedule = new BeginningStockAdapter(stockReportMasterBOS, getActivity(), configurationMasterHelper);
        mSchedule.setBeginningStockAdapterCallback(BeginningStockFragment.this);
        lvWpList.setAdapter(mSchedule);
    }

    @Override
    public void showError() {
        showMessage(getString(R.string.sessionout_loginagain));

    }

    @Override
    public void hideCaseTitle() {
        caseTitle.setVisibility(View.GONE);
    }

    @Override
    public void setCaseTitle(String title) {
        caseTitle.setText(title);
    }

    @Override
    public void hidePieceTitle() {
        pcsTitle.setVisibility(View.GONE);
    }

    @Override
    public void setPieceTitle(String title) {
        pcsTitle.setText(title);

    }

    @Override
    public void setTotalTitle(String total) {
        totalTitle.setText(total);
    }

    @Override
    public void finishActivity() {
        showMessage(getString(R.string.sessionout_loginagain));
        getActivity().finish();
    }

}
