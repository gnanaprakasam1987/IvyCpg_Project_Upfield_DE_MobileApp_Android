package com.ivy.cpg.view.dashboard.sellerdashboard;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ivy.cpg.view.dashboard.DashBoardBO;
import com.ivy.cpg.view.dashboard.DashBoardHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.SKUWiseTargetBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;

public class TotalAchivedFragment extends Fragment {

    private BusinessModel bmodel;

    private View view;
    private ArrayList<SKUWiseTargetBO> skuList;
    private TextView tvTitle, tvValue;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_total_achived, container, false);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Bundle b = getArguments();
        int flex1 = b.getInt("flex1");
        skuList = DashBoardHelper.getInstance(getActivity()).getSkuwiseGraphData();
        tvTitle = (TextView) view.findViewById(R.id.tvTitle);
        tvValue = (TextView) view.findViewById(R.id.tvValue);

        //typefaceApp
        tvTitle.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        tvValue.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.MEDIUM));

        try {
            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.tvTitle).getTag()) != null)
                ((TextView) view.findViewById(R.id.tvTitle))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(view.findViewById(R.id.tvTitle)
                                        .getTag()));
        } catch (Exception e) {
            Commons.printException(e);
        }

        double total_ach = 0;

        if (skuList != null) {
            for (SKUWiseTargetBO skuWiseTargetBO : skuList)
                total_ach += skuWiseTargetBO.getAchieved();
        } else {
            for (DashBoardBO dash : DashBoardHelper.getInstance(getActivity()).getDashChartData()) {
                total_ach += SDUtil.convertToDouble(dash.getKpiIncentive());
            }
        }

        if (flex1 == 1) {
            tvValue.setText(DashBoardHelper.getInstance(getActivity()).getWhole(total_ach + ""));
        } else {
            tvValue.setText(bmodel.formatValue(total_ach));
        }

    }

}