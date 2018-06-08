package com.ivy.cpg.view.reports.eodstockreport;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.print.EODStockReportPreviewScreen;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EODStockReportFragmentRe extends Fragment implements IEodStockView {

    private BusinessModel bmodel;
    private IEodStockModelPresenter modelPresenter;

    @BindView(R.id.list)
    ListView lv;

    @BindView(R.id.print)
    Button btnPrint;

    @BindView(R.id.ll_explist)
    LinearLayout layoutPrint;

    private void initialize() {
        modelPresenter = new EodStockModel(getActivity(), EODStockReportFragmentRe.this);
    }

    private void initializeBusinessModel() {
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeBusinessModel();
        initialize();
        modelPresenter.downloadEodReport();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_report_eod_stock,
                container, false);
        ButterKnife.bind(this, view);
        setUpViews(view);

        modelPresenter.setAdapter();

        if (bmodel.configurationMasterHelper.SHOW_BUTTON_PRINT01) {
            btnPrint.setVisibility(View.VISIBLE);
            layoutPrint.setVisibility(View.VISIBLE);
        }

        btnPrint.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), EODStockReportPreviewScreen.class);
                startActivity(i);
            }
        });

        return view;
    }


    private void setUpViews(View view) {

        if (bmodel.configurationMasterHelper.SHOW_STOCK_REPLACE)
            view.findViewById(R.id.ll_replacement).setVisibility(View.VISIBLE);

        if (bmodel.configurationMasterHelper.SHOW_STOCK_EMPTY)
            view.findViewById(R.id.ll_empty).setVisibility(View.VISIBLE);

        if (bmodel.configurationMasterHelper.SHOW_STOCK_FREE_ISSUED)
            view.findViewById(R.id.ll_free_issued).setVisibility(View.VISIBLE);

        if (bmodel.configurationMasterHelper.CONVERT_EOD_SIH_PS ||
                bmodel.configurationMasterHelper.CONVERT_EOD_SIH_CS ||
                bmodel.configurationMasterHelper.CONVERT_EOD_SIH_OU) {

            if (bmodel.configurationMasterHelper.CONVERT_EOD_SIH_PS) {
                setUpEOD_SIH_PSViews(view, View.VISIBLE);
            } else
                setUpEOD_SIH_PSViews(view, View.GONE);


            if (bmodel.configurationMasterHelper.CONVERT_EOD_SIH_CS) {
                setUpEOD_SIH_CSViews(view, View.VISIBLE);
            } else
                setUpEOD_SIH_CSViews(view, View.GONE);


            if (bmodel.configurationMasterHelper.CONVERT_EOD_SIH_OU) {
                setUpEOD_SIH_OUViews(view, View.VISIBLE);
            } else
                setUpEOD_SIH_OUViews(view, View.GONE);


        } else if (bmodel.configurationMasterHelper.IS_EOD_STOCK_SPLIT) {


            if (bmodel.configurationMasterHelper.SHOW_EOD_OP)
                setUpEOD_SIH_PSViews(view, View.VISIBLE);
            else
                setUpEOD_SIH_PSViews(view, View.GONE);


            if (bmodel.configurationMasterHelper.SHOW_EOD_OC)
                setUpEOD_SIH_CSViews(view, View.VISIBLE);
            else
                setUpEOD_SIH_CSViews(view, View.GONE);


            if (bmodel.configurationMasterHelper.SHOW_EOD_OO)
                setUpEOD_SIH_OUViews(view, View.VISIBLE);
            else
                setUpEOD_SIH_OUViews(view, View.GONE);


        } else {
            hideAllViews(view);
        }
    }

    private void hideAllViews(View view) {

        view.findViewById(R.id.loading_stock_cs_title).setVisibility(View.GONE);
        view.findViewById(R.id.loading_stock_ou_title).setVisibility(View.GONE);

        view.findViewById(R.id.tv_return_cs_title).setVisibility(View.GONE);
        view.findViewById(R.id.tv_return_ou_title).setVisibility(View.GONE);

        view.findViewById(R.id.tv_sold_stock_cs_title).setVisibility(View.GONE);
        view.findViewById(R.id.tv_sold_stock_ou_title).setVisibility(View.GONE);

        view.findViewById(R.id.tv_free_issued_cs_title).setVisibility(View.GONE);
        view.findViewById(R.id.tv_free_issued_ou_title).setVisibility(View.GONE);

        view.findViewById(R.id.tv_replacement_cs_title).setVisibility(View.GONE);
        view.findViewById(R.id.tv_replacement_ou_title).setVisibility(View.GONE);

        view.findViewById(R.id.tv_sih_cs_title).setVisibility(View.GONE);
        view.findViewById(R.id.tv_sih_ou_title).setVisibility(View.GONE);

        view.findViewById(R.id.tv_empty_cs_title).setVisibility(View.GONE);
        view.findViewById(R.id.tv_empty_ou_title).setVisibility(View.GONE);
    }


    private void setUpEOD_SIH_PSViews(View view, int visible) {
        view.findViewById(R.id.loading_stock_pc_title).setVisibility(visible);
        view.findViewById(R.id.tv_return_pc_title).setVisibility(visible);
        view.findViewById(R.id.tv_free_issued_pc_title).setVisibility(visible);
        view.findViewById(R.id.tv_replacement_pc_title).setVisibility(visible);
        view.findViewById(R.id.tv_sih_pc_title).setVisibility(visible);
        view.findViewById(R.id.tv_empty_pc_title).setVisibility(visible);
        view.findViewById(R.id.tv_sold_stock_pc_title).setVisibility(visible);
    }

    private void setUpEOD_SIH_CSViews(View view, int visible) {

        view.findViewById(R.id.loading_stock_cs_title).setVisibility(visible);
        view.findViewById(R.id.tv_return_cs_title).setVisibility(visible);
        view.findViewById(R.id.tv_free_issued_cs_title).setVisibility(visible);
        view.findViewById(R.id.tv_replacement_cs_title).setVisibility(visible);
        view.findViewById(R.id.tv_sih_cs_title).setVisibility(visible);
        view.findViewById(R.id.tv_empty_cs_title).setVisibility(visible);
        view.findViewById(R.id.tv_sold_stock_cs_title).setVisibility(visible);
    }

    private void setUpEOD_SIH_OUViews(View view, int visible) {

        view.findViewById(R.id.loading_stock_ou_title).setVisibility(visible);
        view.findViewById(R.id.tv_return_ou_title).setVisibility(visible);
        view.findViewById(R.id.tv_sold_stock_ou_title).setVisibility(visible);
        view.findViewById(R.id.tv_replacement_ou_title).setVisibility(visible);
        view.findViewById(R.id.tv_sih_ou_title).setVisibility(visible);
        view.findViewById(R.id.tv_empty_ou_title).setVisibility(visible);
        view.findViewById(R.id.tv_sold_stock_ou_title).setVisibility(visible);
    }

    @Override
    public void setAdapter(EodStockAdapter adapter) {
        if (adapter != null)
            lv.setAdapter(adapter);
    }
}