package com.ivy.sd.png.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ivy.countersales.CSsale;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.SchemeBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;

/**
 * Created by hanifa.m on 7/18/2017.
 */

public class CsAllSchemeDetailFragment extends IvyBaseFragment {

    private View rootView;
    private BusinessModel bmodel;
    LinearLayout mainLayout;
    ArrayList<SchemeBO> allSchemeList = new ArrayList<>();
    LinearLayout ll_content;
    TextView lbl_prod_name;

    private LinearLayout.LayoutParams linearlprams, linearlprams2;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_all_scheme_details, container, false);

        setHasOptionsMenu(true);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        ll_content = (LinearLayout) rootView.findViewById(R.id.ll_content);

        lbl_prod_name = (TextView) rootView.findViewById(R.id.tv_prod_lbl);
        lbl_prod_name.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

        createView();

        return rootView;
    }


    @Override
    public void onStart() {
        super.onStart();
        final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            setScreenTitle(
                    "Scheme View");
        }
        setHasOptionsMenu(true);
    }


    private void createView() {

        try {
            allSchemeList = bmodel.schemeDetailsMasterHelper.downLoadAllFreeSchemeDetail();

            LayoutInflater inflater = LayoutInflater.from(getActivity());

            int lastProductPId = 0;
            View cardView = null, productView;
            LinearLayout ll_product_layout = null;

            if (allSchemeList != null)
                for (SchemeBO schemeBo : allSchemeList) {

                    if (lastProductPId != schemeBo.getParentId()) {

                        cardView = inflater.inflate(R.layout.scheme_child_view, null);
                        TextView tv_groupName = (TextView) cardView.findViewById(R.id.tv_groupName);
                        tv_groupName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                        tv_groupName.setText(schemeBo.getGroupName());
                        ll_content.addView(cardView);

                    }


                    ll_product_layout = (LinearLayout) cardView.findViewById(R.id.ll_products);

                    productView = inflater.inflate(R.layout.layout_scheme_pid_list, null);


                    TextView tv_prod_name = (TextView) productView.findViewById(R.id.tv_productname);
                    tv_prod_name.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                    tv_prod_name.setText(
                            schemeBo.getScheme());


                    ll_product_layout.addView(productView);

                    lastProductPId = schemeBo.getParentId();

                }
        } catch (Exception ex) {
            Commons.printException(ex);
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == android.R.id.home) {
            startActivity(new Intent(getActivity(), CSsale.class));
            getActivity().finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
