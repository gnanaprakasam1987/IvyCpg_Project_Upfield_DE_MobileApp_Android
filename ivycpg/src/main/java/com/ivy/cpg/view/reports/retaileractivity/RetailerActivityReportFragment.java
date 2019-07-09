package com.ivy.cpg.view.reports.retaileractivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.Toast;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;

/**
 * Retailer wise Order and time stamp information will be shown.
 */
public class RetailerActivityReportFragment extends IvyBaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        View view = inflater.inflate(R.layout.fragment_retailer_activity_report, container,
                false);

        BusinessModel bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

        RetailerActivityReportHelper retailerActivityReportHelper = new RetailerActivityReportHelper(bmodel);

        ListView listview = view.findViewById(R.id.lvwplist);
        listview.setCacheColorHint(0);

        RetailerActivityAdapter mSchedule = new RetailerActivityAdapter(retailerActivityReportHelper.downloadRetailerActivityReport(), bmodel);
        listview.setAdapter(mSchedule);

        return view;
    }

}
