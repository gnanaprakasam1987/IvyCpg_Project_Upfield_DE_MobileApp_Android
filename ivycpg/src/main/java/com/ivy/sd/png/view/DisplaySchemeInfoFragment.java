package com.ivy.sd.png.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.SchemeBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;

import java.util.ArrayList;

/**
 * Created by Rajkumar on 3/1/18.
 */

public class DisplaySchemeInfoFragment extends IvyBaseFragment {

    BusinessModel businessModel;
    View rootView;
    String mSelectedSchemeId;
    RecyclerView listView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_display_scheme_info, container,
                false);

        businessModel = (BusinessModel) getActivity().getApplicationContext();
        businessModel.setContext(getActivity());


        Bundle extras = getActivity().getIntent().getExtras();
        if (extras != null) {
            mSelectedSchemeId = extras.getString("schemeId");
        }

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews();
    }

    private void initializeViews() {

        TextView label_display_period = (TextView) getView().findViewById(R.id.label_display_period);
        TextView label_booking_period = (TextView) getView().findViewById(R.id.label_booking_period);
        TextView label_qualifier = (TextView) getView().findViewById(R.id.label_qualifiers);

        label_display_period.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        label_booking_period.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        label_qualifier.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

        TextView textView_scheme_desc = (TextView) getView().findViewById(R.id.text_scheme_desc);
        textView_scheme_desc.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        TextView textView_display_period = (TextView) getView().findViewById(R.id.text_display_period);
        TextView textView_booking_period = (TextView) getView().findViewById(R.id.text_booking_period);
        TextView textView_qualifier = (TextView) getView().findViewById(R.id.text_qualifiers);
        textView_display_period.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        textView_booking_period.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        textView_qualifier.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

        for (SchemeBO schemeBO : businessModel.schemeDetailsMasterHelper.getmDisplaySchemeMasterList()) {
            if (schemeBO.getSchemeId().equals(mSelectedSchemeId)) {
                textView_scheme_desc.setText(schemeBO.getScheme());
                textView_display_period.setText(schemeBO.getDisplayPeriodStart() + " - " + schemeBO.getDisplayPeriodEnd());
                textView_booking_period.setText(schemeBO.getBookingPeriodStart() + " - " + schemeBO.getBookingPeriodEnd());
                textView_qualifier.setText(schemeBO.getQualifier());
            }
        }

        getView().findViewById(R.id.view_dotted).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        getView().findViewById(R.id.view_dotted_line).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        TextView text_products_label = (TextView) getView().findViewById(R.id.text_product_title);
        text_products_label.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        listView = (RecyclerView) getView().findViewById(R.id.list);
        listView.setHasFixedSize(true);
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        listView.setLayoutManager(mLayoutManager);

        ArrayList<String> mProductList = businessModel.schemeDetailsMasterHelper.downloadDisplaySchemeProducts(getActivity().getApplicationContext(), mSelectedSchemeId);

        RecyclerViewAdapter mAdapter = new RecyclerViewAdapter(mProductList);
        listView.setAdapter(mAdapter);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }


    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

        private ArrayList<String> items;

        public RecyclerViewAdapter(ArrayList<String> items) {
            this.items = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_display_scheme_info, parent, false);

            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {

            holder.text_product_name.setText(items.get(position));

        }


        @Override
        public int getItemCount() {
            return items.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView text_product_name;

            public ViewHolder(View v) {
                super(v);
                text_product_name = (TextView) v.findViewById(R.id.text_product_name);

                text_product_name.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

            }


        }
    }
}
