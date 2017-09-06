package com.ivy.sd.png.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;

import java.util.ArrayList;

/**
 * Created by mansoor.k on 23-05-2016.
 */
public class MSLUnsoldFragment extends IvyBaseFragment {
    private BusinessModel bmodel;
    private View view;
    private ListView lv_msl;
    private ArrayList<String> mslUnsoldList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        view = inflater.inflate(R.layout.fragment_msl_unsold, container,
                false);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mslUnsoldList = bmodel.dashBoardHelper.loadMSLUnsold(bmodel.retailerMasterBO.getRetailerID());
        initialization();


    }

    private void initialization() {
        lv_msl = (ListView) view.findViewById(R.id.lv_msl);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, mslUnsoldList);
        lv_msl.setAdapter(adapter);

    }

}



