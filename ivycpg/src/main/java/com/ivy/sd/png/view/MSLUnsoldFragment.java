package com.ivy.sd.png.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ivy.cpg.view.dashboard.DashBoardHelper;
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
    private boolean _hasLoadedOnce = false;

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

    private void initializeViews() {
        mslUnsoldList = DashBoardHelper.getInstance(getActivity()).loadMSLUnsold(bmodel.retailerMasterBO.getRetailerID());
        initialization();
    }

    @Override
    public void setUserVisibleHint(boolean isFragmentVisible_) {
        super.setUserVisibleHint(isFragmentVisible_);


        if (this.isVisible()) {
            // we check that the fragment is becoming visible
            isFragmentVisible_ = false;
            if (!isFragmentVisible_ && !_hasLoadedOnce) {
                //run your async task here since the user has just focused on your fragment
                initializeViews();
                _hasLoadedOnce = true;

            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();


    }

    private void initialization() {
        lv_msl = (ListView) view.findViewById(R.id.lv_msl);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, mslUnsoldList);
        lv_msl.setAdapter(adapter);

    }

}



