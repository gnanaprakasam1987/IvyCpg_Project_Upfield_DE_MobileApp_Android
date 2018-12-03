package com.ivy.sd.png.view.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.SBDHelper;

import java.util.HashMap;

/**
 * Created by ramkumard on 12/4/18
 */

public class SBDGapFragment extends IvyBaseFragment {


    BusinessModel bmodel;
    Button button_save_target;
    EditText editText_target;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sbd_gap,
                container, false);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        button_save_target = rootView.findViewById(R.id.button_save_target);
        button_save_target.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        editText_target = rootView.findViewById(R.id.edittext_target);
        editText_target.setText(String.valueOf(bmodel.formatValue(bmodel.getRetailerMasterBO().getDaily_target())));

        button_save_target.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!editText_target.getText().toString().isEmpty()) {
                    if (bmodel.getRetailerMasterBO().getKpiid_day() != 0) {
                        SBDHelper.getInstance(getActivity())
                                .saveDayTarget(bmodel.getRetailerMasterBO().getKpiid_day(),
                                        Double.valueOf(editText_target.getText().toString()),getActivity().getApplicationContext());
                        Toast.makeText(getActivity(), getResources().getString(R.string.saved_successfully), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getActivity(), getResources().getString(R.string.retailer_kpi_not_available), Toast.LENGTH_LONG).show();
                    }
                } else {
                    editText_target.setError("Please Enter");
                    editText_target.requestFocus();
                }

            }
        });

        prepareScreen(rootView);

        return rootView;
    }


    @Override
    public void onStart() {
        super.onStart();
    }


    private void prepareScreen(View rootView){
        HashMap<String,HashMap<String,String>> mSBDGap = SBDHelper.getInstance(getActivity())
                .calculateSBDDistribution(getActivity().getApplicationContext());

        LinearLayout layout_root = rootView.findViewById(R.id.root);


        View productView,groupView;
        LayoutInflater inflater = LayoutInflater.from(getActivity());

        if(mSBDGap.size()>0) {
            for (String groupName : mSBDGap.keySet()) {
                HashMap<String, String> mProductList = mSBDGap.get(groupName);

                groupView = inflater.inflate(R.layout.layout_sbd_gap, null);
                LinearLayout layout_groupView = groupView.findViewById(R.id.layout_group);
                for (String productName : mProductList.keySet()) {

                    productView = inflater.inflate(R.layout.layout_sbd_gap_list_item, null);

                    TextView tv_prod_name = productView.findViewById(R.id.text_productname);
                    tv_prod_name.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                    tv_prod_name.setText(productName);

                    TextView text_quantity = productView.findViewById(R.id.text_quantity);
                    text_quantity.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                    text_quantity.setText(mProductList.get(productName));

                    layout_groupView.addView(productView);
                }

                layout_root.addView(groupView);
            }
        }
    }
}
