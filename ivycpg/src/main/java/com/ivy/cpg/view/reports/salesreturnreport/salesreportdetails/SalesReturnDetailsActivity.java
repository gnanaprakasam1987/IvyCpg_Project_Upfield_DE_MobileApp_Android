package com.ivy.cpg.view.reports.salesreturnreport.salesreportdetails;

import android.database.Cursor;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.ivy.cpg.view.reports.salesreturnreport.SalesReturnReportHelper;
import com.ivy.cpg.view.salesreturn.SalesReturnHelper;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.StringUtils;

import java.util.Vector;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;


public class SalesReturnDetailsActivity extends IvyBaseActivityNoActionBar {
    private Unbinder unbinder;
    private SalesReturnHelper salesReturnHelper;

    @BindView(R.id.recyclerView_salesReport)
    RecyclerView recyclerView;


    @BindView(R.id.txttotal)
    TextView totalQty;

    @BindView(R.id.txttotallines)
    TextView totalLines;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salesreport_details);
        unbinder = ButterKnife.bind(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        // Used to on / off the back arrow icon
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Used to remove the app logo actionbar icon and set title as home
        // (title support click)
        // getSupportActionBar().setDisplayShowHomeEnabled(true);
        // Used to hide the app logo icon from actionbar
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        if (getSupportActionBar() != null)
            setScreenTitle(getResources().getString(R.string.salesreport_details));

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String title = extras.getString("menuName") == null ? "" : extras.getString("menuName");
            // setScreenTitle(title);
        }

        getData();
        setLabelMaserValue();
    }

    private void setLabelMaserValue() {

        BusinessModel businessModel = (BusinessModel) getApplicationContext();


        try {
            if (businessModel.labelsMasterHelper.applyLabels(findViewById(
                    R.id.cqty).getTag()) != null)
                ((TextView) findViewById(R.id.cqty))
                        .setText(businessModel.labelsMasterHelper
                                .applyLabels(findViewById(R.id.cqty)
                                        .getTag()));
            if (businessModel.labelsMasterHelper.applyLabels(findViewById(
                    R.id.piececqty).getTag()) != null)
                ((TextView) findViewById(R.id.piececqty))
                        .setText(businessModel.labelsMasterHelper
                                .applyLabels(findViewById(R.id.piececqty)
                                        .getTag()));

            if (businessModel.labelsMasterHelper.applyLabels(findViewById(
                    R.id.outercqty).getTag()) != null)
                ((TextView) findViewById(R.id.outercqty))
                        .setText(businessModel.labelsMasterHelper
                                .applyLabels(findViewById(R.id.outercqty)
                                        .getTag()));
        } catch (Exception e) {
            Commons.printException(e);
        }


        if (!salesReturnHelper.SHOW_SALES_RET_CASE)
            findViewById(R.id.cqty).setVisibility(View.GONE);
        if (!salesReturnHelper.SHOW_SALES_RET_PCS)
            findViewById(R.id.piececqty).setVisibility(View.GONE);
        if (!salesReturnHelper.SHOW_SALES_RET_OUTER_CASE)
            findViewById(R.id.outercqty).setVisibility(View.GONE);
    }

    private CompositeDisposable mCompositeDisposable;

    private void getData() {
        String uId = "";
        if (getIntent() != null) {
            uId = getIntent().getStringExtra("UID");
            if (getSupportActionBar() != null && !uId.isEmpty())
                setScreenTitle(uId);
        }

        totalValue(uId);

        salesReturnHelper = SalesReturnHelper.getInstance(this);
        salesReturnHelper.loadSalesReturnConfigurations(getApplicationContext());

        SalesReturnReportHelper salesReturnReportHelper = new SalesReturnReportHelper();
        mCompositeDisposable = new CompositeDisposable();

        mCompositeDisposable.add((Disposable) salesReturnReportHelper.getSaleReturnDeliveryDetails(this, uId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(getObserver()));

    }


    public Observer<Vector<SalesReturnDetailsReportBo>> getObserver() {
        return new DisposableObserver<Vector<SalesReturnDetailsReportBo>>() {
            @Override
            public void onNext(Vector<SalesReturnDetailsReportBo> reportBos) {
                setUpAdapter(reportBos);

            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        };
    }

    private void setUpAdapter(Vector<SalesReturnDetailsReportBo> reportBos) {
        SalesReturnReportDetailsAdapter adapter = new SalesReturnReportDetailsAdapter(this, reportBos);
        recyclerView.setAdapter(adapter);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                mLayoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setLayoutManager(mLayoutManager);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

        onBackButtonClick();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackButtonClick();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onBackButtonClick() {
        finish();
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
    }

    private void totalValue(String uId) {

        try {
            DBUtil dbUtil = new DBUtil(this, DataMembers.DB_NAME);
            dbUtil.openDataBase();
            Cursor cursor = dbUtil
                    .selectSQL("SELECT totalQty,srpedited from SalesReturnDetails "
                            + "where uid=" + StringUtils.getStringQueryParam(uId));
            int total = 0;

            int totalLine = 0;
            if (cursor != null) {


                while (cursor.moveToNext()) {
                    total = total + (cursor.getInt(0) * cursor.getInt(1));
                    totalLine = totalLine + 1;
                }
            }
            dbUtil.closeDB();
            totalQty.setText(String.valueOf(total));
            totalLines.setText(String.valueOf(totalLine));
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    protected void onDestroy() {
        unbinder.unbind();
        super.onDestroy();
    }
}
