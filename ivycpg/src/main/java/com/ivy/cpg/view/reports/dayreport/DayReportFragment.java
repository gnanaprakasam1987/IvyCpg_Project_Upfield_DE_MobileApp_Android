package com.ivy.cpg.view.reports.dayreport;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Looper;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class DayReportFragment extends IvyBaseFragment implements DayReportView {

    private DayReportPresenter dayReportModelPresenter;

    @Inject
    public BusinessModel bModel;

    public static final String ZEBRA_3INCH = "3";

    @BindView(R.id.gridview)
    GridView dayReportGrid;

    private Unbinder unbinder;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeBusinessModel();
        initModel();

    }

    private void initModel() {
        dayReportModelPresenter = new DayReportPresenterImpl(getActivity(), DayReportFragment.this, bModel);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        View view = inflater.inflate(R.layout.fragment_daily_report_new, container,
                false);

        unbinder = ButterKnife.bind(this, view);

        if (bModel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(), getResources().getString(R.string.sessionout_loginagain), Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }



        dayReportModelPresenter.downloadData();

        return view;
    }


    private void initializeBusinessModel() {
        bModel = (BusinessModel) getActivity().getApplicationContext();
        bModel.setContext(getActivity());


    }

    //private Connection zebraPrinterConnection;
    private AlertDialog alertDialog;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.menu_dayreport, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (bModel.configurationMasterHelper.IS_DAY_REPORT_PRINT)
            menu.findItem(R.id.menu_print).setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.menu_print) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            customProgressDialog(builder, "Printing....");
            alertDialog = builder.create();
            new Thread(new Runnable() {
                public void run() {
                    Looper.prepare();
                    DayReportPrintHelper dayReportPrintHelper = new DayReportPrintHelper(bModel, getActivity(), alertDialog, dayReportModelPresenter);
                    dayReportPrintHelper.doConnection(ZEBRA_3INCH);
                    Looper.loop();
                    //noinspection ConstantConditions
                    Looper.myLooper().quit();
                }
            }).start();
            alertDialog.show();
            return true;
        }
        return super.onOptionsItemSelected(item);

    }


    @Override
    public void setAdapter(DayReportAdapter dayReportAdapter) {
        dayReportGrid.setAdapter(dayReportAdapter);
    }

    @Override
    public void onDestroy() {

        unbinder.unbind();
        bModel = null;
        dayReportModelPresenter.destroy();
        super.onDestroy();


    }

}
