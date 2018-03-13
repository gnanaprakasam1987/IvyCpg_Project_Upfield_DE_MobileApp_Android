package com.ivy.sd.png.view;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.ivy.sd.png.asean.view.R;


public class OfferPlanningFragment extends Fragment {

    ListView listViewRetailer;

    @Override

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_offer_planning, container, false);

        listViewRetailer = (ListView) view.findViewById(R.id.lv_retailer_list);

        return view;
    }

}
